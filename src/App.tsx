import {View, Text, Pressable, NativeModules} from 'react-native';
import React from 'react';
const App = () => {
  const showBubble = () => {
    NativeModules.BubbleModule.onCreate();
  };

  return (
    <View>
      <Text>App</Text>

      <Pressable
        style={{
          width: 100,
          height: 35,
          justifyContent: 'center',
          alignItems: 'center',
          borderRadius: 10,
          backgroundColor: '#9CC9C9',
        }}
        onPress={() => showBubble()}>
        <Text>Press</Text>
      </Pressable>
    </View>
  );
};

export default App;
