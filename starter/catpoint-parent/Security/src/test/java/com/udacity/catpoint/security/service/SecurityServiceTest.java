package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.FakeImageService;
import com.udacity.catpoint.security.data.*;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.rekognition.endpoints.internal.Value;

import java.awt.image.BufferedImage;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest extends TestCase {
    private SecurityService securityService;
    @Mock
    SecurityRepository securityRepository;
    @Mock
    FakeImageService imageService;
    Sensor sensor_door = new Sensor("Door", SensorType.DOOR);
    Sensor sensor_window = new Sensor("Window", SensorType.WINDOW);
    Sensor sensor_motion = new Sensor("Motion", SensorType.MOTION);

    BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }



    //    #1. Alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @ParameterizedTest
    @ValueSource( strings = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test #1")
    public void alarmArmed_sensorAcivated_alarmStatusPuttoPending(String armingStatus){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.valueOf(armingStatus));
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor_window, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }


    //#2. Alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    @ParameterizedTest
    @ValueSource( strings = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test #2")
    public void alarmArmed_sensorAcivated_alarmStatusPuttoPending_alarmStatusToAlarm(String alArmingStatus) {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.valueOf(alArmingStatus));
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor_window, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //#3. Pending alarm and all sensors are inactive, return to no alarm state.
//    @ParameterizedTest
//    @ValueSource( strings = {"ARMED_AWAY","ARMED_HOME"})
    @Test
    @DisplayName("Test #3")
    public void pendingAlram_allSensorsInactived_returnNoAlarmState() {

//        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor_window.setActive(false);
        securityService.changeSensorActivationStatus(sensor_window, false);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //#4 Alarm is active, change in sensor state should not affect the alarm state.
    @ParameterizedTest
    @ValueSource( booleans = {true, false})
    @DisplayName("Test #4")
    public void testSetAlarmStatus(boolean sensorStatus) {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor_window, sensorStatus);
        assertEquals(securityService.getAlarmStatus(), AlarmStatus.ALARM);
    }


    //#5 A sensor is activated while already active and the system is in pending state, change it to alarm state.
    @Test
    @DisplayName("Test #5")
    public void sensorActivated_whileAlreadyActive_systemPending_changToAlarmState() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor_window.setActive(true);
        securityService.changeSensorActivationStatus(sensor_window,true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }

    //#6 A sensor is deactivated while already inactive, make no changes to the alarm state.
    @ParameterizedTest
    @ValueSource(strings = {"ALARM","NO_ALARM"})
    @DisplayName("Test #6")
    public void sensorDeactivated_whileAlreadyInactive_noChangeAlarmState(String alarmStatus) {
        sensor_window.setActive(Boolean.FALSE);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.valueOf(alarmStatus));
        securityService.changeSensorActivationStatus(sensor_window,false);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }


    //7# If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
    @Test
    @DisplayName("Test #7")
    public void identifiesAnImageACat_whileSystemArmedHome_putSystemIntoAlarm() {

        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(img);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //#8 If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    @Test
    @DisplayName("Test #8")
    public void identifiesAnImageACat_NotContainACat_changeStatusToNoAlarm_asLongAsSensorsAreNotActive () {
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(false);
        sensor_window.setActive(Boolean.FALSE);
        securityService.processImage(img);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //#9 If the system is disarmed, set the status to no alarm.
    @Test
    @DisplayName("Test #9")
    public void systemDisarmed_setAlarmStatusNoAlarm(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);

    }


    //#10 If the system is armed, reset all sensors to inactive.
    @ParameterizedTest
    @ValueSource( strings = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test #10")
    public void systemArmed_allSensorsInactive(String alarmingStatus ) {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.changeSensorActivationStatus(sensor_door, true);
        securityService.changeSensorActivationStatus(sensor_window, true);
        securityService.changeSensorActivationStatus(sensor_motion, true);
        securityService.setArmingStatus(ArmingStatus.valueOf(alarmingStatus));
        assertEquals(0, securityService.getActiveSensors().size());

    }
    //#11 If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    @Test
    @DisplayName("Test #11")
    public void systemArmedHome_and_whileCameraShowsACat_setAlarmToAlarm() {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(Boolean.TRUE);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.processImage(img);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

}
