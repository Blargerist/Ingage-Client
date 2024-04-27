# Ingage Client: A Java client for streamers to configure game and software integrations

> [!CAUTION]
> This is an early personal project. While it is built on multiple years of experience with stream integration software, it may have bugs or issues and breaking changes that require things like configs to be remade are not unlikely.

## Installation
> [!CAUTION]
> The Ingage Client currently only supports Windows.

### Windows
Download the latest installer from the [Releases](https://github.com/Blargerist/Ingage-Client/releases) and run the exe.

## Usage

### Adding accounts
Navigate to the Accounts screen using the tab at the top of the app. Here you can view currently authorized accounts and add new ones.

#### Twitch
From the Accounts screen, press the Add Twitch Account button. This will open a Twitch OAuth page. Authorize to listen to events from the account.

#### Streamlabs
From the Accounts screen, press the Add Streamlabs Account button. This will open a textbox where you can paste a Socket API Token. Press enter to listen to events from the account.

To find your Socket API Token, log in to the Streamlabs website, Navigate to Accounts -> Settings -> API Settings and press the Copy button on Your Socket API Token. Pressing this button will make the token partially visible, do not do this on stream.

### Adding Integrations
New integrations can be added to the Ingage Client while it is running by downloading them from wherever they may be hosted and running them, whether by running a game where the mod is installed or running a standalone application. The integration will connect to the Ingage Client app and transfer its information, making it available for configuration.

### Configuring Events
Navigate to the Events screen using the tab at the top of the app. Here you can add new Profiles, which contain a set of Events and can be toggled on and off, making it possible to have different profiles for various games or situations.

#### Events
Events are made up of Conditions and Effects. If any condition is met, effects will be sent to their corresponding integrations.
