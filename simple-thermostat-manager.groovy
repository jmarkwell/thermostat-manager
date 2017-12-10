/**
 *  Simple Thermostat Manager
 *  Build 2017121003
 *
 *  Copyright 2017 Jordan Markwell
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  ChangeLog:
 *     
 *      20171210:
 *          01: Corrected a mistake in the help paragraph.
 *          02: Reconfigured the placement of the help text.
 *          03: Added the ability to have Simple Thermostat Manager ignore a temperature threshold by manually setting it to 0.
 *
 *      20171125:
 *          01: Reverted system back to using user defined boundaries.
 *          02: Changed fanMode state check to check for "auto" instead of "fanAuto".
 *
 *      Earlier:
 *          Creation
 *          Modified to use established thermostat setPoints rather than user defined boundaries.
 */
definition(
    name: "Simple Thermostat Manager",
    namespace: "jmarkwell",
    author: "Jordan Markwell",
    description: "Adjusts thermostat mode in response to changes in temperature that exceed user defined cooling and heating thermostat setpoints.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png@2x.png"
)

preferences {
    section {
        input "thermostat", "capability.thermostat", title: "Thermostat", multiple: false, required: true
        paragraph "When the temperature rises higher than the cooling threshold, Simple Thermostat Manager will set cooling mode. Recommended value: 75"
        input name: "coolingThreshold", title: "Cooling Threshold", type: "number", required: false
        paragraph "When the temperature falls below the heating threshold, Simple Thermostat Manager will set heating mode. Recommended value: 70"
        input name: "heatingThreshold", title: "Heating Threshold", type: "number", required: false
        paragraph title: "Tips:", "If you set the cooling threshold at the lowest setting you use in your modes and you set the heating threshold at the highest setting you use in your modes, you will not need to create two instances of Simple Thermostat Manager."
        paragraph "If you want to use Simple Thermostat Manager to set cooling mode only or to set heating mode only, remove the value for the threshold that you want to be ignored or set it to 0."
        input name: "disable", title: "Disable Simple Thermostat Manager", type: "bool", defaultValue: false, required: true
        input name: "setFan", title: "Maintain Auto Fan Mode", type: "bool", defaultValue: true, required: true
        input name: "debug", title: "Debug Logging", type: "bool", defaultValue: false, required: true
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
     subscribe thermostat, "temperature", tempHandler
}

def tempHandler(evt) {
    def currentTemp     = thermostat.currentValue("temperature")
    def coolingSetpoint = thermostat.currentValue("coolingSetpoint")
    def heatingSetpoint = thermostat.currentValue("heatingSetpoint")
    def currentMode     = thermostat.currentValue("thermostatMode")
    def fanMode         = thermostat.currentValue("thermostatFanMode")
   
    if (debug) {
        log.debug "Simple Thermostat Manager Fan Mode: ${fanMode}"
        log.debug "Simple Thermostat Manager Mode: ${currentMode}"
        log.debug "Simple Thermostat Manager Heating Setpoint: ${heatingSetpoint}"
        log.debug "Simple Thermostat Manager Cooling Setpoint: ${coolingSetpoint}"
        log.debug "Simple Thermostat Manager Temperature: ${currentTemp}"
    }
   
    if ( (!disable) && (setFan) && (fanMode != "auto") ) {
        log.debug "Simple Thermostat Manager setting fan mode auto."
        thermostat.fanAuto()
    }
    
    // Hello Home only sets the setPoint for the active thermostat mode.
    if ( (!disable) && (currentMode != "cool") && coolingThreshold && ( Math.round(currentTemp) > Math.round(coolingThreshold) ) ) {
        log.debug "Simple Thermostat Manager setting cooling mode."
        thermostat.cool()
    } else if ( (!disable) && (currentMode != "heat") && heatingThreshold && ( Math.round(currentTemp) < Math.round(heatingThreshold) ) ) {
        log.debug "Simple Thermostat Manager setting heating mode."
        thermostat.heat()
    } else if (debug) {
        log.debug "Simple Thermostat Manager standing by."
    }
}
