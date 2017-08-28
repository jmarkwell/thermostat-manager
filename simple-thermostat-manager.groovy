/**
 *  Simple Thermostat Manager
 *  Build 2017082801
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
 *      Earlier:
 *          Creation
 *          Modified to use established thermostat setPoints rather than user defined boundaries
 *      
 */
definition(
    name: "Simple Thermostat Manager",
    namespace: "jmarkwell",
    author: "Jordan Markwell",
    description: "Adjusts thermostat mode in response to changes in temperature that exceed user defined cooling and heating thermostat setpoints.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png@2x.png")

preferences {
    section {
        input "thermostat", "capability.thermostat", title: "Thermostat", multiple: "false", required: "true"
        paragraph "The Safety Threshold value defines the number of degrees in temperature to allow beyond your cooling and heating SetPoints before Simple Thermostat Manager changes the mode. Most hardware thermostats use a value between 3 - 7 for this setting."
        input name: "safety", title: "Safety Threshold", type: "number", defaultValue: 1, required: "true"
        input name: "disable", title: "Disable Simple Thermostat Manager", type: "bool", defaultValue: "false", required: "true"
        input name: "setFan", title: "Maintain Auto Fan Mode", type: "bool", defaultValue: "true", required: "true"
        input name: "debug", title: "Debug Logging", type: "bool", defaultValue: "false", required: "true"
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
    
    if ( (!disable) && (setFan) && (fanMode != "fanAuto") ) {
        log.debug "Simple Thermostat Manager setting fan mode auto."
        thermostat.fanAuto()
    }
    
    if ( (!disable) && ( Math.round(currentTemp) > ( Math.round(coolingSetpoint) + safety ) ) ) {
        if (currentMode != "cool") {
            log.debug "Simple Thermostat Manager setting cooling mode."
            thermostat.cool()
        }
    } else if ( (!disable) && ( Math.round(currentTemp) < ( Math.round(heatingSetpoint) - safety ) ) ) {
        if (currentMode != "heat") {
            log.debug "Simple Thermostat Manager setting heating mode."
            thermostat.heat()
        }
    } else if (debug) {
            log.debug "Simple Thermostat Manager standing by."
    }
}
