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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest extends TestCase {
    private SecurityService securityService;
    @Mock
    SecurityRepository securityRepository;
    @Mock
    FakeImageService imageService;
    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    Sensor sensor_window = new Sensor("WINDOW", SensorType.WINDOW);

//    Alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    @DisplayName("Test #1")
    public void alarmArmed_sensorAcivated_alarmStatusPuttoPending(ArmingStatus armingStatus){
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);


        securityService.changeSensorActivationStatus(sensor_window, true);


    }

    public void testAddStatusListener() {
    }

    public void testRemoveStatusListener() {
    }

    public void testSetAlarmStatus() {
    }

    public void testChangeSensorActivationStatus() {
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