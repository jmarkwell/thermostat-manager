/**
 *  Thermostat Manager
 *  Build 2017121304
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
 *      20171213:
 *          01: Standardized optional Smart Home Monitor based setPoint enforcement with corresponding preference
 *              settings.
 *          02: Added notification capabilities.
 *          03: Renamed from, "Simple Thermostat Manager" to, "Thermostat Manager".
 *          04: Corrected an incorrect setPoint preference variable.
 *
 *      20171212:
 *          01: Added Hello Home mode value and Smart Home Monitor status value to debug logging.
 *          02: Added a preliminary form of setPoint enforcement.
 *
 *      20171210:
 *          01: Corrected a mistake in the help paragraph.
 *          02: Reconfigured the placement of the help text.
 *          03: Added the ability to have Simple Thermostat Manager ignore a temperature threshold by manually setting
 *              it to 0.
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
    name: "Thermostat Manager",
    namespace: "jmarkwell",
    author: "Jordan Markwell",
    description: "Automatically changes the thermostat mode in response to changes in temperature that exceed user defined thresholds.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png@2x.png"
)

preferences {
    page(name: "mainPage")
    page(name: "setPointPage")
    page(name: "notificationPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", install: true, uninstall: true) {
        section() {
            paragraph title: "Thermostat Manager", "Automatically changes the thermostat mode in response to changes in temperature that exceed user defined thresholds."
        }
        section("Settings") {
            input "thermostat", "capability.thermostat", title: "Thermostat", multiple: false, required: true
            paragraph "When the temperature rises higher than the cooling threshold, Thermostat Manager will set cooling mode. Recommended value: 75"
            input name: "coolingThreshold", title: "Cooling Threshold", type: "number", required: false
            paragraph "When the temperature falls below the heating threshold, Thermostat Manager will set heating mode. Recommended value: 70"
            input name: "heatingThreshold", title: "Heating Threshold", type: "number", required: false
            paragraph title: "Tips:", "If you set the cooling threshold at the lowest setting you use in your modes and you set the heating threshold at the highest setting you use in your modes, you will not need to create two instances of Thermostat Manager."
            paragraph "If you want to use Thermostat Manager to set cooling mode only or to set heating mode only, remove the value for the threshold that you want to be ignored or set it to 0."
            href "setPointPage", title: "Smart Home Monitor Based SetPoint Enforcement"
            href "notificationPage", title: "Notification Settings"
            input name: "setFan", title: "Maintain Auto Fan Mode", type: "bool", defaultValue: true, required: true
            input name: "debug", title: "Debug Logging", type: "bool", defaultValue: false, required: true
            input name: "disable", title: "Disable Thermostat Manager", type: "bool", defaultValue: false, required: true
        }
        section() {
            mode(title: "Set for specific mode(s)")
            label(title: "Assign a name", required: false)
        }
    }
}

def setPointPage() {
    dynamicPage(name: "setPointPage", title: "Smart Home Monitor Based SetPoint Enforcement") {
        section() {
            paragraph "These optional settings allow you use Thermostat Manager to set your thermostat's cooling and heating setPoints based on the status of Smart Home Monitor; SmartThings' built-in security system. SetPoints will be set only when a thermostat mode change occurs (e.g. heating to cooling) and only the setPoint for the incoming mode will be set (e.g. A change from heating mode to cooling mode would prompt the cooling setPoint to be set)."
        }
        section("Disarmed Status") {
            input name: "offCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
            input name: "offHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
        }
        section("Armed (stay) Status") {
            input name: "stayCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
            input name: "stayHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
        }
        section("Armed (away) Status") {
            input name: "awayCoolingSetPoint", title: "Cooling SetPoint", type: "number", required: false
            input name: "awayHeatingSetPoint", title: "Heating SetPoint", type: "number", required: false
        }
    }
}

def notificationPage() {
    dynamicPage(name:"notificationPage", title:"Notification Settings") {
        section() {
            input(name: "recipients", title: "Select Notification Recipients", type: "contact", required: false) {
                input name: "phone", title: "Select Text Message Notification Recipients", type: "phone", required: false
            }
        }
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
    def thermostatMode  = thermostat.currentValue("thermostatMode")
    def fanMode         = thermostat.currentValue("thermostatFanMode")
    def homeMode        = location.mode
    def securityStatus  = location.currentValue("alarmSystemStatus")
   
    if (debug) {
        log.debug "Thermostat Manager - Smart Home Monitor Status: ${securityStatus}"
        log.debug "Thermostat Manager - Hello Home Mode: ${homeMode}"
        log.debug "Thermostat Manager - Fan Mode: ${fanMode}"
        log.debug "Thermostat Manager - Mode: ${thermostatMode}"
        log.debug "Thermostat Manager - Heating Setpoint: ${heatingSetpoint}"
        log.debug "Thermostat Manager - Cooling Setpoint: ${coolingSetpoint}"
        log.debug "Thermostat Manager - Temperature: ${currentTemp}"
    }
   
    if ( (!disable) && (setFan) && (fanMode != "auto") ) {
        log.debug "Thermostat Manager setting fan mode auto."
        thermostat.fanAuto()
    }
    
    // Hello Home only sets the setPoint for the active thermostat mode.
    if ( (!disable) && (thermostatMode != "cool") && coolingThreshold && ( Math.round(currentTemp) > Math.round(coolingThreshold) ) ) {
        logNNotify("Thermostat Manager setting cooling mode.")
        thermostat.cool()
        
        if ( (securityStatus == "off") && (offCoolingSetPoint) ) {
            logNNotify("Thermostat Manager is setting the cooling setPoint to ${offCoolingSetPoint}.")
            thermostat.setCoolingSetpoint(offCoolingSetPoint)
        } else if ( (securityStatus == "stay") && (stayCoolingSetPoint) ) {
            logNNotify("Thermostat Manager is setting the cooling setPoint to ${stayCoolingSetPoint}.")
            thermostat.setCoolingSetpoint(stayCoolingSetPoint)
        } else if ( (securityStatus == "away") && (awayCoolingSetPoint) ) {
            logNNotify("Thermostat Manager is setting the cooling setPoint to ${awayCoolingSetPoint}.")
            thermostat.setCoolingSetpoint(awayCoolingSetPoint)
        }
    } else if ( (!disable) && (thermostatMode != "heat") && heatingThreshold && ( Math.round(currentTemp) < Math.round(heatingThreshold) ) ) {
        logNNotify("Thermostat Manager setting heating mode.")
        thermostat.heat()
        
        if ( (securityStatus == "off") && (offHeatingSetPoint) ) {
            logNNotify("Thermostat Manager is setting the heating setPoint to ${offHeatingSetPoint}.")
            thermostat.setHeatingSetpoint(offHeatingSetPoint)
        } else if ( (securityStatus == "stay") && (stayHeatingSetPoint) ) {
            logNNotify("Thermostat Manager is setting the heating setPoint to ${stayHeatingSetPoint}.")
            thermostat.setHeatingSetpoint(stayHeatingSetPoint)
        } else if ( (securityStatus == "away") && (awayHeatingSetPoint) ) {
            logNNotify("Thermostat Manager is setting the heating setPoint to ${awayHeatingSetPoint}.")
            thermostat.setHeatingSetpoint(awayHeatingSetPoint)
        }
    } else if (debug) {
        log.debug "Thermostat Manager standing by."
    }
}

def logNNotify(message) {
    log.debug message
    if (location.contactBookEnabled && recipients) {
        sendNotificationToContacts(message, recipients)
    } else if (phone) {
        sendSms(phone, message)
    }
}
