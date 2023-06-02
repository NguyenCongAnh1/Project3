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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.rekognition.endpoints.internal.Value;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest extends TestCase {
    private SecurityService securityService;
    @Mock
    SecurityRepository securityRepository;
    @Mock
    FakeImageService imageService;
    Sensor sensor_window = new Sensor("WINDOW", SensorType.WINDOW);
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
    @ParameterizedTest
    @ValueSource( strings = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test #3")
    public void pendingAlram_allSensorsInactived_returnNoAlarmState(String alArmingStatus) {

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.valueOf(alArmingStatus));
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor_window, true);
        securityService.changeSensorActivationStatus(sensor_window, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
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
        sensor_window.setActive(false);
        sensor_window.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor_window,true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }

    public void testProcessImage() {
    }

    public void testGetAlarmStatus() {
    }

    public void testGetSensors() {
    }

    public void testAddSensor() {
    }

    public void testRemoveSensor() {
    }

    public void testGetArmingStatus() {
    }
}
