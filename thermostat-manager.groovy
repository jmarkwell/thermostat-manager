/**
 *  Thermostat Manager
 *  Build 2017151310
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
 *      20171215:
 *          01: Added capability to automatically set thermostat to "off" mode in the case that user selected contact
 *              sensors have remained open for longer than a user specified number of minutes.
 *          02: Added ability to override Thermostat Manager by manually setting the thermostat to "off" mode.
 *          03: Added push notification capability.
 *          04: Modified logging behavior. Rearranged menus. General code cleanup.
 *          05: Added ability to disable Smart Home Monitor based setPoint enforcement without having to remove user
 *              defined values.
 *          06: Added ability to disable notifications without having to remove contacts.
 *          07: Missed a comma.
 *          08: Modifying notification messages.
 *          09: Converting tempHandler's event.value to integer.
 *          10: Returned to using thermostat.currentValue("temperature") instead of event.value.toInteger() for the
 *              currentTemp variable in the tempHandler() function.
 *
 *      20171213:
 *          01: Standardized optional Smart Home Monitor based setPoint enforcement with corresponding preference
 *              settings.
 *          02: Added notification capabilities.
 *          03: Renamed from, "Simple Thermostat Manager" to, "Thermostat Manager".
 *          04: Corrected an incorrect setPoint preference variable.
 *          05: Edited the text of the text notification preference setting.
 *          06: Menu cleanup.
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
    page(name: "energySaverPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Thermostat Manager", install: true, uninstall: true) {
        section() {
            paragraph "Automatically changes the thermostat mode in response to changes in temperature that exceed user defined thresholds."
        }
        section("Main Configuration") {
            input "thermostat", "capability.thermostat", title: "Thermostat", multiple: false, required: true
            paragraph "When the temperature rises higher than the cooling threshold, Thermostat Manager will set cooling mode. Recommended value: 75"
            input name: "coolingThreshold", title: "Cooling Threshold", type: "number", required: false
            paragraph "When the temperature falls below the heating threshold, Thermostat Manager will set heating mode. Recommended value: 70"
            input name: "heatingThreshold", title: "Heating Threshold", type: "number", required: false
        }
        section("Tips") {
            paragraph "If you set the cooling threshold at the lowest setting you use in your modes and you set the heating threshold at the highest setting you use in your modes, you will not need to create multiple instances of Thermostat Manager."
            paragraph "If you want to use Thermostat Manager to set cooling mode only or to set heating mode only, remove the value for the threshold that you want to be ignored or set it to 0."
        }
        section("Optional Settings") {
            input name: "setFan", title: "Maintain Auto Fan Mode", type: "bool", defaultValue: true, required: true
            input name: "manualOverride", title: "Allow Manual Thermostat Off to Override Thermostat Manager", type: "bool", defaultValue: false, required: true
            input name: "debug", title: "Debug Logging", type: "bool", defaultValue: false, required: true
            input name: "disable", title: "Disable Thermostat Manager", type: "bool", defaultValue: false, required: true
            
            href "setPointPage", title: "Smart Home Monitor Based SetPoint Enforcement"
            href "energySaverPage", title: "Energy Saver"
            href "notificationPage", title: "Notification Settings"
            
            label(title: "Assign a name", required: false)
            mode(title: "Set for specific mode(s)")
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
        section() {
            input name: "disableSHMSPEnforce", title: "Disable Smart Home Monitor Based SetPoint Enforcement", type: "bool", defaultValue: false, required: true
        }
    }
}

def notificationPage() {
    dynamicPage(name:"notificationPage", title:"Notification Settings") {
        section() {
            input(name: "recipients", title: "Select Notification Recipients", type: "contact", required: false) {
                input name: "phone", title: "Enter Phone Number of Text Message Notification Recipient", type: "phone", required: false
            }
            input name: "pushNotify", title: "Send Push Notifications", type: "bool", defaultValue: false, required: true
            input name: "disableNotifications", title: "Disable Notifications", type: "bool", defaultValue: false, required: true
        }
    }
}

def energySaverPage() {
    dynamicPage(name:"energySaverPage", title:"Energy Saver") {
        section() {
            paragraph "Energy Saver will temporarily pause the thermostat (by placing it in \"off\" mode) in the case that any selected contact sensors are left open for a specified number of minutes."
            input name: "contact", title: "Contact Sensors", type: "capability.contactSensor", multiple: true, required: false
            input name: "openContactMinutes", title: "Minutes", type: "number", required: false
            input name: "disableEnergySaver", title: "Disable Energy Saver", type: "bool", defaultValue: false, required: true
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    state.clear()
    
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
     subscribe thermostat, "temperature", tempHandler
     if (!disableEnergySaver) {
        subscribe contact, "contact.open", contactOpenHandler
        subscribe contact, "contact.closed", contactClosedHandler
     }
}

def tempHandler() {
    def currentTemp     = thermostat.currentValue("temperature")
    def coolingSetpoint = thermostat.currentValue("coolingSetpoint")
    def heatingSetpoint = thermostat.currentValue("heatingSetpoint")
    def thermostatMode  = thermostat.currentValue("thermostatMode")
    def fanMode         = thermostat.currentValue("thermostatFanMode")
    def homeMode        = location.mode
    def securityStatus  = location.currentValue("alarmSystemStatus")
    
    if (debug) {
        if (!disableEnergySaver && contact) {
            log.debug "Thermostat Manager - At least one contact is open: ${contact.currentValue("contact").contains("open")}"
            if (state.lastThermostatMode) { log.debug "Thermostat Manager is currently paused." }
        }
        log.debug "Thermostat Manager - Smart Home Monitor Status: ${securityStatus}"
        log.debug "Thermostat Manager - Hello Home Mode: ${homeMode}"
        log.debug "Thermostat Manager - Fan Mode: ${fanMode}"
        log.debug "Thermostat Manager - Mode: ${thermostatMode}"
        log.debug "Thermostat Manager - Cooling Setpoint: ${coolingSetpoint} | Heating Setpoint: ${heatingSetpoint}"
        log.debug "Thermostat Manager - Temperature: ${currentTemp}"
    }
   
    if ( (!disable) && (setFan) && (fanMode != "auto") ) {
        logNNotify("Thermostat Manager setting fan mode auto.")
        thermostat.fanAuto()
    }
    
    // Hello Home only sets the setPoint for the active thermostat mode.
    if ( (!disable) && ( ( !manualOverride && (thermostatMode != "cool") ) || ( manualOverride && (thermostatMode == "heat") ) ) && coolingThreshold && ( Math.round(currentTemp) > Math.round(coolingThreshold) ) ) {
        logNNotify("Thermostat Manager - The temperature has risen to ${currentTemp}. Setting cooling mode.")
        thermostat.cool()
        
        if (!disableSHMSPEnforce) {
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
        }
    } else if ( (!disable) && ( ( !manualOverride && (thermostatMode != "heat") ) || ( manualOverride && (thermostatMode == "cool") ) ) && heatingThreshold && ( Math.round(currentTemp) < Math.round(heatingThreshold) ) ) {
        logNNotify("Thermostat Manager - The temperature has fallen to ${currentTemp}. Setting heating mode.")
        thermostat.heat()
        
        if (!disableSHMSPEnforce) {
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
        }
    } else if (debug) {
        log.debug "Thermostat Manager standing by."
    }
}

def logNNotify(message) {
    log.debug message
    if (!disableNotifications) {
        if (location.contactBookEnabled && recipients) {
            sendNotificationToContacts(message, recipients)
        } else if (phone) {
            sendSms(phone, message)
        }
        
        if (pushNotify) {
            sendpush(message)
        }
    }
}

def contactOpenHandler() {
    def thermostatMode = thermostat.currentValue("thermostatMode")
    
    if (debug) {
        log.debug "Thermostat Manager - A contact has been opened."
    }
    
    if ( (thermostatMode != "off") && (!state.openContact) ) {
        // If the thermostat is not off and all of the contacts were closed previously.
        state.openContact = true
        runIn( (openContactMinutes * 60), openContactPause )
        log.debug "Thermostat Manager - A contact has been opened. Initiating countdown to thermostat pause."
    }
}

def contactClosedHandler() {
    if (debug) {
        log.debug "Thermostat Manager - A contact has been closed."
    }
    
    if (state.openContact) {
        // If there was an open contact previously.
        if ( !contact.currentValue("contact").contains("open") ) {
            // All of the contacts have been closed. Discontinue any existing countdown.
            log.debug "Thermostat Manager - All contacts have been closed. Discontinuing any existing thermostat pause countdown."
            unschedule(openContactPause)
            
            if (state.lastThermostatMode) {
                // If the thermostat is currently paused, restore it to its previous state.
                if (state.lastThermostatMode == "cool") {
                    logNNotify("Thermostat Manager - All contacts have been closed. Restoring cooling mode.")
                    thermostat.cool()
                } else if (state.lastThermostatMode == "heat") {
                    logNNotify("Thermostat Manager - All contacts have been closed. Restoring heating mode.")
                    thermostat.heat()
                }
                state.lastThermostatMode = null
            }
            state.openContact = false
        }
    }
}

def openContactPause() {
    state.lastThermostatMode = thermostat.currentValue("thermostatMode")
    logNNotify("Thermostat Manager is turning the thermostat off temporarily due to an open contact.")
    thermostat.off()
}
