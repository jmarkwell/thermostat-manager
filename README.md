# Thermostat Manager
Thermostat Manager is a SmartThings SmartApp that provides automated control over smart thermostats.

You can set temperature thresholds that define what mode you would like your thermostat to be in (heat/cool/etc.) for a specified temperature range. You use Energy Saver features to have your thermostat turn off temporarily if a contact sensor (such as a door or a window) stays open too long. Mode Based Temperature Enforcement allows you to set a specified temperature based on a SmartThings mode change (such as home or away mode). Emergency Heat Settings allow you to set emergency heat mode based on the temperature reading of an outdoor temperature sensor. You can also configure Notification Settings to give you a push notification or a text message when Thermostat Manager makes a change.

If you need assistance, would like to request new features, or would like to learn more, check out [the SmartThings Community forum](https://community.smartthings.com/t/release-thermostat-manager-an-alternative-to-thermostat-mode-director/).

## Standard Installation

To Install **Thermostat Manager** Smart App:

1. Login to the [SmartThings IDE](https://account.smartthings.com/).
2. Click **My SmartApps**.
3. Click the **New SmartApp** button.
4. Select the **From Code** tab.
5. Copy all of the code from the [thermostat-manager.groovy](https://raw.githubusercontent.com/jmarkwell/thermostat-manager/master/smartapps/jmarkwell/thermostat-manager.src/thermostat-manager.groovy) file into the box.
6. Click the **Create** button.
7. Click the **Save** button.
8. Click the **Publish** button and click **For Me**.

## GitHub Installation

If you would like to add the Thermostat Manager GitHub repository to your SmartThings IDE for easy updates, follow these steps:

1. Login to the [SmartThings IDE](https://account.smartthings.com/).
2. Click **My SmartApps**.
3. Click the **Settings** button.
4. Click **Add a new repository**.
5. Fill out the new line in the form using the following information:

* **Owner**: jmarkwell
* **Name**: thermostat-manager
* **Branch**: master

6. Click **Save**.

You can now update to the latest build by following these steps:

1. Click **Update from Repo**.
2. Click **thermostat-manager (master)**.
3. Select the **Publish** checkbox and click the **Execute Update** button.

If your Thermostat Manager build is current, it will appear in black text in your list of SmartApps.
