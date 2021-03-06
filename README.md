# 52°North Sensor Things API (STA)

## Documentation
* Standard: [OGC SensorThings API Part I: Sensing](https://github.com/opengeospatial/sensorthings)
* Additional Features: [STA Github Wiki](https://github.com/52North/sensorweb-server-sta/wiki)

## Setup
### Docker 
A dockerfile for building the App is provided in the root directory.
A complete demo setup is provided via a [docker-compose file](https://github.com/52North/sensorweb-server-sta/docker-compose.yml)

## Testing
A [Postman](https://www.getpostman.com/) Collection with a multitude of demo requests is provided in the `etc` subdirectory.


## Conformance Test Suite Status:

| Conformance Class                     | Reference | Implemented |Test Status |
|:--------------------------------------|:---------:|:-----------:|-----------:|
| Sensing Core                          | A.1       | Yes         |   5 / 6    |
| Filtering Extension                   | A.2       | Yes         |   0 / 8    |
| Create-Update-Delete                  | A.3       | Yes         |   8 / 8    |
| Batch Requests                        | A.4       | No          |   0 / ?    |
| MultiDatastream Extension             | A.5       | No          |   0 / ?    |
| DataArray Extension                   | A.6       | No          |   0 / ?    |
| Observation Creation via MQTT         | A.7       | Yes         |   1 / 1    |
| Receiving Updates via MQTT            | A.8       | Yes         |   5 / 5    |

## Notes:
#### MQTT Extension
 - Subscription is only possible on Topics (aka REST-Endpoints) that exist. This is checked on Subscription creation. SUBACK Message is send regardless.
