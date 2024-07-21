#!/bin/bash

# Update system and install required packages
sudo pacman -Syu --noconfirm
sudo pacman -S wget --noconfirm

# Create directory for the client
mkdir -p ~/runescape-client

# Download the RuneScape client
echo "Downloading RuneScape client..."
wget -O ~/runescape-client/runescape-launcher.tar.gz "https://www.runescape.com/downloads/runescape-launcher-linux.tar.gz"

# Extract the client
echo "Extracting RuneScape client..."
tar -xzf ~/runescape-client/runescape-launcher.tar.gz -C ~/runescape-client

# Move the launcher to a system-wide location (optional)
sudo mv ~/runescape-client/runescape-launcher /opt/runescape-launcher

# Create a desktop entry (optional)
echo "[Desktop Entry]
Name=RuneScape
Exec=/opt/runescape-launcher/runescape-launcher
Icon=/opt/runescape-launcher/runescape-launcher.png
Type=Application
Categories=Game;" | sudo tee /usr/share/applications/runescape.desktop

echo "RuneScape client installed successfully."
