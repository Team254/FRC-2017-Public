set -e
PACKAGES=()
DO_INSTALL=0
if ! opkg list-installed | grep -F 'libjpeg8 - 8d-r1.20'; then
    PACKAGES+=("opkg_cache/libjpeg8_8d-r1.20_cortexa9-vfpv3.ipk")
    DO_INSTALL=1
else
    echo "libjpeg8 already installed"
fi
if ! opkg list-installed | grep -F 'libgcc1 - 4.9.2-r0.73'; then
    PACKAGES+=("opkg_cache/libgcc1_4.9.2-r0.73_cortexa9-vfpv3.ipk")
    DO_INSTALL=1
else
    echo "libgcc1 already installed"
fi
if ! opkg list-installed | grep -F 'libstdc++6 - 4.9.2-r0.71'; then
    PACKAGES+=("opkg_cache/libstdc++6_4.9.2-r0.71_cortexa9-vfpv3.ipk")
    DO_INSTALL=1
else
    echo "libstdc++6 already installed"
fi
if ! opkg list-installed | grep -F 'media-ctl - 1.6.2-r0.3'; then
    PACKAGES+=("opkg_cache/media-ctl_1.6.2-r0.3_cortexa9-vfpv3.ipk")
    DO_INSTALL=1
else
    echo "media-ctl already installed"
fi
if ! opkg list-installed | grep -F 'libv4l - 1.6.2-r0.3'; then
    PACKAGES+=("opkg_cache/libv4l_1.6.2-r0.3_cortexa9-vfpv3.ipk")
    DO_INSTALL=1
else
    echo "libv4l already installed"
fi
if ! opkg list-installed | grep -F 'mjpg-streamer - 2017.0.0'; then
    PACKAGES+=("opkg_cache/mjpg-streamer_2017.0.0_cortexa9-vfpv3.ipk")
    DO_INSTALL=1
else
    echo "mjpg-streamer already installed"
fi
if ! opkg list-installed | grep -F 'v4l-utils - 1.6.2-r0.3'; then
    PACKAGES+=("opkg_cache/v4l-utils_1.6.2-r0.3_cortexa9-vfpv3.ipk")
    DO_INSTALL=1
else
    echo "v4l-utils already installed"
fi
if [ "${DO_INSTALL}" == "0" ]; then
    echo "No packages to install."
else
    opkg install  ${PACKAGES[@]}
fi