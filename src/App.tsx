import {View, Text, NativeModules, Switch} from 'react-native';
import React, {useEffect, useState} from 'react';
const App = () => {
  const [isEnabled, setIsEnabled] = useState<boolean>(false);

  // Dùng method onReceiveNotify('Hello Ae') để hiển thị thông báo !
  // Tham số là nội dung thông báo

  useEffect(() => {
    if (isEnabled) {
      NativeModules.BubbleModule.onCreate();
      NativeModules.BubbleModule.onReceiveNotify('Hello ae');
    } else {
      NativeModules.BubbleModule.hideNotifyHead();
    }
  }, [isEnabled]);

  const toggleSwitch = () => setIsEnabled(previousState => !previousState);

  return (
    <View
      style={{
        flex: 1,
        alignItems: 'center',
        paddingVertical: 15,
        backgroundColor: '#9CD7CD',
      }}>
      <Text
        style={{
          fontSize: 18,
          fontWeight: 'bold',
          color: 'white',
        }}>
        Android Bubble Notify
      </Text>

      <Switch
        trackColor={{false: '#767577', true: '#81b0ff'}}
        thumbColor={'#f4f3f4'}
        ios_backgroundColor="#3e3e3e"
        onValueChange={toggleSwitch}
        value={isEnabled}
      />
    </View>
  );
};

export default App;
