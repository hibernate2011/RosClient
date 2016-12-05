### RosClient: Android app for ROS
Android communicate with ROS(Robot Operating System),based on [rosbridge protocol](https://github.com/RobotWebTools/rosbridge_suite/blob/groovy-devel/ROSBRIDGE_PROTOCOL.md),see more in my blog http://xxhong.net/ (In Chinese)

### Features
1. Enum all ROS nodes,service,topics
2. Show params in service or topics
3. Subscribe or publish ros topic
4. Call ros service
5. Process topic "/cmd_vel", control the movement of robot
6. Process topic "/map", show the map of SLAM,like rviz

### ScreenShots
![](https://github.com/hibernate2011/RosClient/blob/master/screenshot/1.jpg)
![](https://github.com/hibernate2011/RosClient/blob/master/screenshot/2.jpg)
![](https://github.com/hibernate2011/RosClient/blob/master/screenshot/3.jpg)

### Use Library
- EventBus
- ButterKnife
- json-simple
- java_websocket
- ROSBridgeClient

### License
Copyright 2016 xxhong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.