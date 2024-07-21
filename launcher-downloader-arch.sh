#!/bin/bash

# Install required packages
sudo pacman -Syu --noconfirm
sudo pacman -S wget wine winetricks --noconfirm

# Create directory for the launcher
mkdir -p ~/runescape-launcher

# Download the Jagex Launcher Installer
echo "Downloading Jagex Launcher Installer..."
wget -O ~/runescape-launcher/Jagex_Launcher_Installer.exe "https://cdn.jagex.com/Jagex%20Launcher%20Installer.exe"

# Install necessary components for Internet Explorer
echo "Installing necessary Wine components..."
winetricks -q ie8

# Install the launcher using Wine
echo "Installing Jagex Launcher..."
wine ~/runescape-launcher/Jagex_Launcher_Installer.exe

# Check common installation paths for the launcher
LAUNCHER_PATH=""

if [ -f "$HOME/.wine/drive_c/Program Files (x86)/Jagex Launcher/JagexLauncher.exe" ]; then
  LAUNCHER_PATH="$HOME/.wine/drive_c/Program Files (x86)/Jagex Launcher/JagexLauncher.exe"
elif [ -f "$HOME/.wine/drive_c/Program Files/Jagex Launcher/JagexLauncher.exe" ]; then
  LAUNCHER_PATH="$HOME/.wine/drive_c/Program Files/Jagex Launcher/JagexLauncher.exe"
else
  echo "Jagex Launcher not found in expected locations."
  exit 1
fi

# Move the launcher to the appropriate directory
mkdir -p ./data/launchers/win
cp "$LAUNCHER_PATH" ./data/launchers/win/origina.exe

echo "Jagex Launcher downloaded and installed successfully."
