// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

#include <cstdio>
#include <string>
#include <thread>
#include <vector>

#include <networktables/NetworkTableInstance.h>
#include <networktables/NetworkTable.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgcodecs.hpp>
#include <vision/VisionPipeline.h>
#include <vision/VisionRunner.h>
#include <wpi/StringRef.h>
#include <wpi/json.h>
#include <wpi/raw_istream.h>
#include <wpi/raw_ostream.h>

#include "cameraserver/CameraServer.h"

static const char* configFile = "/boot/frc.json";

unsigned int team;
bool server = false;

std::shared_ptr<nt::NetworkTable> smartDashboard;

auto start_time = std::chrono::high_resolution_clock::now();

struct CameraConfig {
  std::string name;
  std::string path;
  wpi::json config;
  wpi::json streamConfig;
};

struct SwitchedCameraConfig {
  std::string name;
  std::string key;
};

std::vector<CameraConfig> cameraConfigs;
std::vector<SwitchedCameraConfig> switchedCameraConfigs;
std::vector<cs::VideoSource> cameras;
std::vector<cs::MjpegServer> servers;

wpi::raw_ostream& ParseError() {
  return wpi::errs() << "config error in '" << configFile << "': ";
}

bool ReadCameraConfig(const wpi::json& config) {
  CameraConfig c;

  // name
  try {
    c.name = config.at("name").get<std::string>();
  } catch (const wpi::json::exception& e) {
    ParseError() << "could not read camera name: " << e.what() << '\n';
    return false;
  }

  // path
  try {
    c.path = config.at("path").get<std::string>();
  } catch (const wpi::json::exception& e) {
    ParseError() << "camera '" << c.name
                 << "': could not read path: " << e.what() << '\n';
    return false;
  }

  // stream properties
  if (config.count("stream") != 0) c.streamConfig = config.at("stream");

  c.config = config;

  cameraConfigs.emplace_back(std::move(c));
  return true;
}

bool ReadConfig() {
  // open config file
  std::error_code ec;
  wpi::raw_fd_istream is(configFile, ec);
  if (ec) {
    wpi::errs() << "could not open '" << configFile << "': " << ec.message()
                << '\n';
    return false;
  }

  // parse file
  wpi::json j;
  try {
    j = wpi::json::parse(is);
  } catch (const wpi::json::parse_error& e) {
    ParseError() << "byte " << e.byte << ": " << e.what() << '\n';
    return false;
  }

  // top level must be an object
  if (!j.is_object()) {
    ParseError() << "must be JSON object\n";
    return false;
  }

  // team number
  try {
    team = j.at("team").get<unsigned int>();
  } catch (const wpi::json::exception& e) {
    ParseError() << "could not read team number: " << e.what() << '\n';
    return false;
  }

  // ntmode (optional)
  if (j.count("ntmode") != 0) {
    try {
      auto str = j.at("ntmode").get<std::string>();
      wpi::StringRef s(str);
      if (s.equals_lower("client")) {
        server = false;
      } else if (s.equals_lower("server")) {
        server = true;
      } else {
        ParseError() << "could not understand ntmode value '" << str << "'\n";
      }
    } catch (const wpi::json::exception& e) {
      ParseError() << "could not read ntmode: " << e.what() << '\n';
    }
  }

  // cameras
  try {
    for (auto&& camera : j.at("cameras")) {
      if (!ReadCameraConfig(camera)) return false;
    }
  } catch (const wpi::json::exception& e) {
    ParseError() << "could not read cameras: " << e.what() << '\n';
    return false;
  }

  return true;
}

struct CameraData{
  cs::UsbCamera camera; 
  cs::MjpegServer server;
};

CameraData StartCamera(const CameraConfig& config) {
  wpi::outs() << "Starting camera '" << config.name << "' on " << config.path
              << '\n';
  auto inst = frc::CameraServer::GetInstance();
  cs::UsbCamera camera{config.name, config.path};
  auto server = inst->StartAutomaticCapture(camera);

  camera.SetConfigJson(config.config);
  camera.SetConnectionStrategy(cs::VideoSource::kConnectionKeepOpen);

  if (config.streamConfig.is_object())
    server.SetConfigJson(config.streamConfig);

  return CameraData{camera, server};
}

void startNetworkTables() {
  auto networkTablesInstance = nt::NetworkTableInstance::GetDefault();
  networkTablesInstance.StartClientTeam(team);
  networkTablesInstance.StartDSClient(); 
  smartDashboard = networkTablesInstance.GetTable("SmartDashboard");
}

class MyPipeline : public frc::VisionPipeline {
  public:
    void Process(cv::Mat& mat) override {
      auto duration = std::chrono::high_resolution_clock::now() - start_time;
      start_time = std::chrono::high_resolution_clock::now();
      std::cout << 1 / (duration.count() / 1e9) << std::endl;
    }
};

int main(int argc, char* argv[]) {
  if (argc >= 2) configFile = argv[1];

  // read configuration
  if (!ReadConfig()) return EXIT_FAILURE;

  // // start NetworkTables
  // auto ntinst = nt::NetworkTableInstance::GetDefault();
  // if (server) {
  //   wpi::outs() << "Setting up NetworkTables server\n";
  //   ntinst.StartServer();
  // } else {
  //   wpi::outs() << "Setting up NetworkTables client for team " << team << '\n';
  //   ntinst.StartClientTeam(team);
  //   ntinst.StartDSClient();
  // }

  // start cameras
  for (const auto& config : cameraConfigs){
      CameraData data = StartCamera(config);
      cameras.emplace_back(data.camera);
      servers.emplace_back(data.server);
  }

  std::cout << "Started the cameras" << std::endl;

  startNetworkTables();

  if (cameras.size() >= 1) {
    std::thread([&] {
      frc::VisionRunner<MyPipeline> runner(cameras[0], new MyPipeline(),
                                           [&](MyPipeline &pipeline) {
        // do something with pipeline results
      });
      /* something like this for GRIP:
      frc::VisionRunner<MyPipeline> runner(cameras[0], new grip::GripPipeline(),
                                           [&](grip::GripPipeline& pipeline) {
        ...
      });
       */
      runner.RunForever();
    }).detach();
  }
  // do {
  //   videoCapture.open(deviceID, apiID);
  // } while (!videoCapture.isOpened());



  // videoCapture.open(deviceID);

  std::cout << "Started the capture" << std::endl;

}

