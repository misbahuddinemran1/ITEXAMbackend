-- পুরোনো push-token কলাম আর নতুন device-token মডেলের মিল
ALTER TABLE user_devices
    ALTER COLUMN device_token DROP NOT NULL;

-- অ্যাপ থেকে platform ছোট হাতের (android) আসে, তাই কড়া CHECK সরানো হলো
ALTER TABLE user_devices
    DROP CONSTRAINT IF EXISTS chk_user_devices_platform;
