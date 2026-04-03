# Overview

This is a tool for the testing and development of Android apps that use GPS. It allows the user to set a single location on the map to mock, or build a route to follow with multiple points.

<img src="images/screenshots/map_screen_empty.png" alt="Mock locations screen" width="400" >

<br />

# Intended Use

This app is intended **for development and testing purposes only**.
It uses Android’s official mock location APIs and requires the app to be
set as the system’s mock location provider.

It is not intended to bypass app restrictions, cheat in games,
or evade location-based safeguards in third-party services.

Other apps may be able to detect that a mock location provider is being used.

# Requirements

- Android 12 (API 31) or later
- Developer Options enabled
- This app set as the system mock location app
- Location permission granted
- Notification permission (Android 13+ recommended)

# Background Behavior

- Mocking continues if the app is closed
- A persistent notification is shown while mocking is active when the notification permission is granted
- If the service is stopped unexpectedly, mocking resumes when the app is reopened
- Stopping mocking returns the device to its real GPS location

# Permissions Explained

- **Location** – Required to show your real location, and mocked location while mocking
- **Notifications** – Required to display a persistent notification while mocking is active
- **Developer Options / Mock Location App** – Required by Android to allow location simulation

# App Setup

When attempting to perform an action for the first time that requires permissions, prompts for those permissions are shown.

<img src="images/screenshots/location_permission_prompt.png" alt="Location permission prompt" width="400" >
<img src="images/screenshots/notification_permission_prompt.png" alt="Notification permission prompt" width="400" >
<img src="images/screenshots/developer_options_prompt.png" alt="Developer options prompt" width="400" >
<img src="images/screenshots/mock_location_app_prompt.png" alt="Mock location prompt" width="400" >

<br />

# Controls

### Start mocking
<img src="images/components/start_button.png" alt="Start button" width="64" >

Starts the mock location service. This service will continue running until the stop button is pressed. If the mocking service stops unexpectedly, it will resume in the same place when the app is opened again.

### Stop mocking
<img src="images/components/stop_button.png" alt="Stop button" width="64" >

Stops the mock location service. Your device will resume using its actual GPS location.

### Pause mocking
<img src="images/components/pause_button.png" alt="Pause button" width="64" >

Pauses the movement of the route that is currently running. The mock location service remains active, showing you at that location on the route until the start or stop mocking buttons are pressed.

### Add point
<img src="images/components/add_point_button.png" alt="Add point button" width="64" >

Adds a point on the map at the location of the crosshairs. This button is not visible when the "use crosshairs" setting is disabled.

### Saved routes
<img src="images/components/saved_routes_button.png" alt="Saved routes button" width="64" >

Shows a dialog for the routes you have saved, and an option to save the current route on your map.

### Clear route
<img src="images/components/clear_route_button.png" alt="Clear route button" width="64" >

Removes all points on the map.

### Remove point
<img src="images/components/remove_point_button.png" alt="Remove point button" width="64" >

Removes the most recently placed point on the map.

### Expanded controls
<img src="images/components/expanded_controls_button.png" alt="Expanded controls button" height="64" >

Opens expanded controls section.

<br />

# Location mocking

Mocking a point keeps your location in the same place until it is stopped. If the notification permission is granted, a notification is shown as long as the mocking service is running. The notification has a button to stop mocking your location. The mocking continues if the app is dismissed, but in the event that the service is stopped unexpectedly, the mocking is resumed in the same place when the app is opened again.

<img src="images/screenshots/mocking_point.png" alt="Mocking point" width="400" >
<img src="images/screenshots/mocking_point_notification.png" alt="Mocking notification" width="400" >

<br />

Adding more points builds a route. This can be done by using the "add point" button to add a point at the crosshairs location, or by long pressing on the map.

<img src="images/screenshots/add_route_point.png" alt="Add route point" width="400" >

<br />

Pressing start starts the mock location service, and it follows the route built at the given speed, measured in meters per second (m/s). Tapping on the tab at bottom center opens the expanded controls, which has a slider to adjust the speed that the route is followed when mocking. When mocking a route, the pause button is available. This button pauses the movement of the device's mocked location. The location appears as that location on the map until the route is resumed, or mocking is stopped. If the mocking service is stopped unexpectedly, it will resume where the route left off when the app is opened again.

<img src="images/screenshots/expanded_controls_route.png" alt="Expanded controls while mocking a route" width="400" >

<br />

# Saving routes
Tapping the "saved routes" button opens a dialog showing your saved routes. Tapping on "Save Route" navigates to the next page, where the route is given a name and saved. There cannot be multiple routes with the same name.

<img src="images/screenshots/no_routes_dialog.png" alt="Saved routes dialog with no routes" width="400" >
<img src="images/screenshots/naming_route_dialog.png" alt="Naming route dialog with empty input" width="400" >
<img src="images/screenshots/naming_route_dialog_named.png" alt="Naming route dialog with ILCB to Zachry" width="400" >
<img src="images/screenshots/saved_routes_dialog.png" alt="Saved routes dialog with route" width="400" >

<br />

Tapping and holding on a saved route in the list selects the route, and shows a button to delete the selected routes.

<img src="images/screenshots/selected_saved_routes.png" alt="Selected saved routes" width="400" >

<br />

# Settings

### Use crosshairs
This is enabled by default. Turning it off will remove the crosshairs marker that is shown in the center of the map screen. With it off, the "add point" button will not show, so adding points to the map is only done by long pressing on the map.

### Clear route on stop
This is enabled by default. With it on, the route or point being mocked will be cleared when the stop button is pressed or the route is completed.
