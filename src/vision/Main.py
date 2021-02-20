#!/usr/bin/env	python3

#	Copyright	(c)	FIRST	and	other	WPILib	contributors.
#	Open	Source	Software;	you	can	modify	and/or	share	it	under	the	terms	of
#	the	WPILib	BSD	license	file	in	the	root	directory	of	this	project.

import json
import time
import sys

from cscore import CameraServer,	VideoSource,	UsbCamera,	MjpegServer
from networktables import NetworkTablesInstance,	NetworkTables
import threading
import numpy as np
import cv2
import ctypes
from math import tan

#			JSON	format:
#			{
#							"team":	<team	number>,
#							"ntmode":	<"client"	or	"server",	"client"	if	unspecified>
#							"cameras":	[
#											{
#															"name":	<camera	name>
#															"path":	<path,	e.g.	"/dev/video0">
#															"pixel	format":	<"MJPEG",	"YUYV",	etc>			//	optional
#															"width":	<video	mode	width>														//	optional
#															"height":	<video	mode	height>												//	optional
#															"fps":	<video	mode	fps>																		//	optional
#															"brightness":	<percentage	brightness>				//	optional
#															"white	balance":	<"auto",	"hold",	value>	//	optional
#															"exposure":	<"auto",	"hold",	value>						//	optional
#															"properties":	[																										//	optional
#																			{
#																							"name":	<property	name>
#																							"value":	<property	value>
#																			}
#															],
#															"stream":	{																														//	optional
#																			"properties":	[
#																							{
#																											"name":	<stream	property	name>
#																											"value":	<stream	property	value>
#																							}
#																			]
#															}
#											}
#							]
#							"switched	cameras":	[
#											{
#															"name":	<virtual	camera	name>
#															"key":	<network	table	key	used	for	selection>
#															//	if	NT	value	is	a	string,	it's	treated	as	a	name
#															//	if	NT	value	is	a	double,	it's	treated	as	an	integer	index
#											}
#							]
#			}

configFile = "/boot/frc.json"


class CameraConfig:
    pass


team = None
server = False
cameraConfigs = []
cameras = []
servers = []

#	HSV	Filter	values
min_hue = 0
min_sat = 0
min_val = 220
max_hue = 180
max_sat = 255
max_val = 255

#	Data	of	setup
target_height = 1.5
camera_height = 0.72

size_reference = 260

horizontal_fov = 62.2

res = [1640, 922]


class thread(threading.Thread):
    def __init__(self,	name,	target=0):
        threading.Thread.__init__(self)
        self.name = name
        self.target = target

    def run(self):
        try:
            if self.name == "cvSinkGetter":
                time.sleep(2)
                self.cvSink = cs.getVideo()
            elif self.name == "watchdog":
                self.cvSink = 0
                print("started watchdog")
                time.sleep(4)
                print("Killed process")
                self.target.raise_exception()
        finally:
            print('ended')

    def get_id(self):

        #	returns	id	of	the	respective	thread
        if hasattr(self,	'_thread_id'):
            return self._thread_id
        for id,	thread in threading._active.items():
            if thread is self:
                return id

    def raise_exception(self):
        thread_id = self.get_id()
        res = ctypes.pythonapi.PyThreadState_SetAsyncExc(thread_id, ctypes.py_object(SystemExit))
        if res > 1:
            ctypes.pythonapi.PyThreadState_SetAsyncExc(thread_id,	0)
            print('Exception	raise	failure')

    def join(self):
        threading.Thread.join(self)
        return self.cvSink


def removeArrayInnerBraces(array):
    output = []
    for element in array:
        output.append([element[0][0],	element[0][1]])
    return output


def toAimingSystem(point):
    x = (point[0] - (res[0]/2)) / (res[0] / 2)
    y = (point[1] - (res[1]/2)) / (res[1] / 2)
    return [x,	y]


def toAbsSystem(point):
    x = int(point[0] + 1 * (res[0]/2))
    y = int(point[1] + 1 * (res[0]/2))
    return [x,	y]


def getMidPoint(pointList):
    x,	y = 0, 0
    for point in pointList:
        x += point[0]
        y += point[1]
    x,	y = x / len(pointList), y / len(pointList)
    return [x,	y]


def getDistance(point1,	point2):
    return ((point1[0] - point2[0])**2 + (point1[1] - point2[1])**2)**0.5

def getMiddleLineLengthStraight(distance):
    return 689.356*(distance ** -0.99836)

def getTargetAngle(MiddleLineLengthDifference):
    targetAngle = -0.002182 * (MiddleLineLengthDifference ** 2) + 0.631977 * MiddleLineLengthDifference + 9.22611
    if targetAngle >= 13: 
        return targetAngle
    else:
        return 0

def parseError(str):
    """Report	parse	error."""
    print("config	error	in	'" + configFile + "': " + str, file=sys.stderr)


def readCameraConfig(config):
    """Read	single	camera	configuration."""
    cam = CameraConfig()

    #	name
    try:
        cam.name = config["name"]
    except KeyError:
        parseError("could	not	read	camera	name")
        return False

    #	path
    try:
        cam.path = config["path"]
    except KeyError:
        parseError("camera	'{}':	could	not	read	path".format(cam.name))
        return False

    #	stream	properties
    cam.streamConfig = config.get("stream")

    cam.config = config

    cameraConfigs.append(cam)
    return True


def readConfig():
    """Read	configuration	file."""
    global team
    global server

    #	parse	file
    try:
        with open(configFile,	"rt",	encoding="utf-8") as f:
            j = json.load(f)
    except OSError as err:
        print("could	not	open	'{}':	{}".format(
            configFile,	err),	file=sys.stderr)
        return False

    #	top	level	must	be	an	object
    if not isinstance(j,	dict):
        parseError("must	be	JSON	object")
        return False

    #	team	number
    try:
        team = j["team"]
    except KeyError:
        parseError("could	not	read	team	number")
        return False

    #	ntmode	(optional)
    if "ntmode" in j:
        str = j["ntmode"]
        if str.lower() == "client":
            server = False
        elif str.lower() == "server":
            server = True
        else:
            parseError("could	not	understand	ntmode	value	'{}'".format(str))

    #	cameras
    try:
        cameras = j["cameras"]
    except KeyError:
        parseError("could	not	read	cameras")
        return False
    for camera in cameras:
        if not readCameraConfig(camera):
            return False

    return True


def startCamera(config):
    """Start	running	the	camera."""
    print("Starting	camera	'{}'	on	{}".format(config.name,	config.path))
    inst = CameraServer.getInstance()
    camera = UsbCamera(config.name, config.path)
    server = inst.startAutomaticCapture(camera=camera, return_server=True)

    camera.setConfigJson(json.dumps(config.config))
    camera.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen)

    if config.streamConfig is not None:
        server.setConfigJson(json.dumps(config.streamConfig))

    return camera,	inst


def startNetworkTables():
    networkTablesInstance = NetworkTablesInstance.getDefault()

    print("Setting	up	NetworkTables	client	for	team	{}".format(team))

    condition = threading.Condition()
    notified = [False]

    def connectionListener(connected,	info):
        print(info,	';	Connected=%s' % connected)
        with condition:
            notified[0] = True
            condition.notify()

    NetworkTables.initialize(server="10.64.17.2")
    NetworkTables.addConnectionListener(
        connectionListener,	immediateNotify=True)

    with condition:
        print("waiting")
        if not notified[0]:
            condition.wait()

    networkTablesInstance.startClientTeam(team)
    networkTablesInstance.startDSClient()
    dashboard = networkTablesInstance.getTable('SmartDashboard')

    print("Connected")

    return networkTablesInstance,	dashboard


if __name__ == "__main__":
    if len(sys.argv) >= 2:
        configFile = sys.argv[1]

    #	read	configuration
    if not readConfig():
        sys.exit(1)

    networkTablesInstance,	dashboard = startNetworkTables()

    dashboard.putBoolean('connected',	False)

    for config in cameraConfigs:
        cam,	server = startCamera(config)
        cameras.append(cam)
        servers.append(server)

    cs = servers[0]

    print("getting	Sink")
    #	cvSink	=	cs.getVideo()
    cvSinkGetter = thread("cvSinkGetter")
    watchdog = thread("watchdog", cvSinkGetter)
    cvSinkGetter.start()
    watchdog.start()
    cvSink = cvSinkGetter.join()
    watchdog.join()

    #	print("starting	Streams")
    outputStream	=	cs.putVideo("Processed_Image",	res[0],	res[1])
    #	binaryStream	=	cs.putVideo("Binary	Image",	res[0],	res[1])

    print("Grabbed	stream")

    img = np.zeros((res[1], res[0], 3), dtype=np.uint8)

    kernel = np.ones((5, 5), dtype=np.uint8)
    erode_kernel = np.ones((3, 3), dtype=np.uint8)

    startTime = time.time()

    #	loop	forever
    while True:
        if dashboard.getBoolean('connected',	False) == False:
            if time.time() - startTime >= 10:
                print("Connection	Timeout")
                sys.exit()

        targetInView = False
        s_time = time.time()

        frame_time,	img = cvSink.grabFrame(img)
        #	if	frame_time	==	0:
        #					outputStream.notifyError(cvSink.getError())
        #					continue

        hsv_img = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
        binary_img = cv2.inRange(
            hsv_img,	(min_hue,	min_sat,	min_val),	(max_hue,	max_sat,	max_val))

        binary_img = cv2.erode(binary_img, erode_kernel, iterations=3)

        _,	contours,	_ = cv2.findContours(
            binary_img,	cv2.RETR_EXTERNAL,	cv2.CHAIN_APPROX_SIMPLE)

        if len(contours) >= 2:
            largest = 0
            largest_difference = 0
            second = 0
            second_difference = 0
            locations = []
            heights = np.array([])
            distances = np.array([])

            for contour in contours:
                rect = cv2.minAreaRect(contour)
                box = cv2.boxPoints(rect)
                box = np.int0(box)
                y_differences = np.array([])
                for i in range(1,	3):
                    y_differences = np.append(
                        y_differences,	abs(box[0][1] - box[i][1]))

                if np.max(y_differences) > largest_difference:
                    second_difference = largest_difference
                    second = largest
                    largest = box
                    largest_difference = np.max(y_differences)
                elif np.max(y_differences) > second_difference:
                    second_difference = np.max(y_differences)
                    second = box

            boxes = np.array([largest, second])
            y_differences = np.array([largest_difference, second_difference])
            side = 0

            for box in boxes:
                try:
                    cv2.drawContours(img,	[box],	-1,	(0,	0,	255),	thickness=1)

                    midpoint = getMidPoint(box)
                    locations.append(midpoint)

                    cv2.circle(img,	(int(midpoint[0]),	int(
                        midpoint[1])),	4,	(255,	255,	255))
                except:
                    print("no	boxes")
            
            side = (locations[0][0] > locations[1][0]) * 2 - 1

            #	cv2.putText(img,	"Length	of	line	1:		"	+	str(int(y_differences[0])),	(0,	70),	cv2.FONT_HERSHEY_SIMPLEX,	1,	(255,	255,	255))
            #	cv2.putText(img,	"Length	of	line	2:		"	+	str(int(y_differences[1])),	(0,	100),	cv2.FONT_HERSHEY_SIMPLEX,	1,	(255,	255,	255))
            np.sort(heights)

            for i in range(2):
                distances = np.append(distances, ((
                    size_reference / y_differences[i])**2 - (target_height - camera_height)**2)**0.5)

            distance = (distances[0] + distances[1])/2
            robotAngle = toAimingSystem(getMidPoint(locations))[
                0] * (horizontal_fov/2)
            lineLength = (abs(locations[0][0] - locations[1][0])**2 + abs(locations[0][1] - locations[1][1])**2)**0.5
            straightLineLength = getMiddleLineLengthStraight(distance)
            targetAngle = getTargetAngle(abs(straightLineLength - lineLength))
            cv2.putText(img,	"Distance_total:    " + str(round(distance, 3)),
                        (0,	250),	cv2.FONT_HERSHEY_SIMPLEX,	1,	(255,	255,	255))
            cv2.putText(img,	"Robot	Viewing	dir(rel	to	target):    " + str(round(toAimingSystem(getMidPoint(
                locations))[0] * (horizontal_fov/2), 3)), (0, 280), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255))
            cv2.putText(img, "Line Length:  " + str(round(lineLength, 2)), (0, 310), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255))
            cv2.putText(img, "Straight Line Length: " + str(round(straightLineLength, 1)), (0, 340), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255))
            cv2.putText(img, "TargetAngle: " + str(round(targetAngle, 1)), (0, 370), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255))
            cv2.putText(img, "Viewing Side: " + str(round(side, 1)), (0, 400), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255))
            cv2.line(img, (int(locations[0][0]), int(locations[0][1])), (int(locations[1][0]), int(locations[1][1])), (0, 255, 0), thickness=2)

            if distance <= 4.5:
                targetInView = True

        if targetInView == False:
            distance = 0
            robotAngle = 0

        processing_time = time.time() - s_time
        fps = round(1 / processing_time, 3)
        cv2.putText(img,	"FPS:     " + str(fps), (0, 40),
                    cv2.FONT_HERSHEY_SIMPLEX,	1,	(0,	0,	255))

        outputStream.putFrame(img)
        #	binaryStream.putFrame(binary_img)
        dashboard.putNumber('distance',	distance)
        dashboard.putBoolean('targetInView',	targetInView)
        dashboard.putNumber('robotAngle',	robotAngle)
        dashboard.putBoolean('currentValues',	True)
        dashboard.putNumber('targetAngle', targetAngle)
        dashboard.putNumber('viewingSide', side)
