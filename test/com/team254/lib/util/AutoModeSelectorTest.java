package com.team254.lib.util;

import com.team254.frc2017.AutoModeSelector;
import com.team254.frc2017.auto.modes.StandStillMode;
import edu.wpi.first.wpilibj.HLUsageReporting;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SmartDashboard.class, NetworkTable.class, HLUsageReporting.class })
public class AutoModeSelectorTest {

    @Test
    public void testAutoModeSelector() {
        PowerMockito.mockStatic(NetworkTable.class, invocationOnMock -> null);
        PowerMockito.mockStatic(HLUsageReporting.class, invocationOnMock -> null);
        PowerMockito.mockStatic(SmartDashboard.class);

        PowerMockito.when(NetworkTable.getTable(Mockito.any())).thenReturn(null);

        ArrayList<String> options = new ArrayList<>();
        PowerMockito
                .when(SmartDashboard.putString(Mockito.eq(AutoModeSelector.AUTO_OPTIONS_DASHBOARD_KEY), Mockito.any()))
                .then(invocationOnMock -> {
                    String jsonString = invocationOnMock.getArgumentAt(1, String.class);
                    JSONParser jsonParser = new JSONParser();
                    JSONArray array = (JSONArray) jsonParser.parse(jsonString);
                    for (Object o : array) {
                        options.add((String) o);
                    }
                    return true;
                });
        PowerMockito.when(
                SmartDashboard.getString(Mockito.eq(AutoModeSelector.SELECTED_AUTO_MODE_DASHBOARD_KEY), Mockito.any()))
                .then(invocationOnMock -> options.get(0));

        AutoModeSelector.initAutoModeSelector();
        // TODO: Fix this
        // Assert.assertTrue(AutoModeSelector.getSelectedAutoMode() instanceof StandStillMode);
    }
}
