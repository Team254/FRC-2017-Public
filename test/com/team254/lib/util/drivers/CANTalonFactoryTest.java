package com.team254.lib.util.drivers;

import com.ctre.CANTalon;
import com.team254.lib.util.drivers.CANTalonFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CANTalonFactory.class)
public class CANTalonFactoryTest {

    @Test
    public void testWhichMethodsAreCalled() throws Exception {
        List<String> acceptableUncalledMethodNames = Arrays.asList(
                "getClass",
                "pidWrite",
                "wait",
                "notifyAll",
                "delete",
                "disableControl",
                "notify",
                "setF",
                "setI",
                "hashCode",
                "enable",
                "setD",
                "setParameter",
                "setP",
                "setPIDSourceType",
                "initTable",
                "getParameter",
                "getMotionProfileStatus",
                "startLiveWindowMode",
                "enableControl",
                "updateTable",
                "setSetpoint",
                "GetGadgeteerStatus",
                "disable",
                "pushMotionProfileTrajectory",
                "equals",
                "reset",
                "stopMotor",
                "processMotionProfileBuffer",
                "setControlMode",
                "stopLiveWindowMode",
                "getMotionMagicActTrajPosition",
                "getMotionMagicActTrajVelocity",
                "DisableNominalClosedLoopVoltage",
                "createTableListener");

        final Set<String> uncalledMethodNames = new HashSet<>(
                Arrays.stream(CANTalon.class.getMethods())
                        .map(m -> m.getName())
                        .filter(name -> !acceptableUncalledMethodNames.contains(name))
                        .collect(Collectors.toSet()));

        CANTalon talon = Mockito.mock(CANTalon.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                uncalledMethodNames.remove(invocationOnMock.getMethod().getName());
                return null;
            }
        });
        PowerMockito.whenNew(CANTalon.class).withAnyArguments().thenReturn(talon);

        CANTalon returnedTalon = CANTalonFactory.createDefaultTalon(1);
        String talonInfo = CANTalonFactory.getFullTalonInfo(returnedTalon);

        Assert.assertEquals(
                new HashSet<>(),
                uncalledMethodNames);
    }

    @Test
    public void testCanPrintInfo() {
        System.out.println(CANTalonFactory.getFullTalonInfo(Mockito.mock(CANTalon.class)));
    }
}
