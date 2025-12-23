package msku.ceng.travelogue.viewmodel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import msku.ceng.travelogue.Travel;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class WhereIveBeenViewModelTest {
/*
    private WhereIveBeenViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new WhereIveBeenViewModel();
    }

    // to test -> creating map pins from a list of travels

    @Test
    public void testCreateMapPins() {
        // a fake travel list
        Travel travel1 = new Travel();
        travel1.setId("1");
        travel1.setCountry("USA");
        travel1.setCity("New York");

        Travel travel2 = new Travel();
        travel2.setId("2");
        travel2.setCountry("France");
        travel2.setCity("Paris");

        List<Travel> travels = new ArrayList<>();
        travels.add(travel1);
        travels.add(travel2);

        // list -> mappin
        List<MapPin> mapPins = viewModel.createMapPins(travels);

        // mappin list
        List<MapPin> expectedMapPins = new ArrayList<>();
        expectedMapPins.add(new MapPin("1", "USA", "New York"));
        expectedMapPins.add(new MapPin("2", "France", "Paris"));

        // check if they are the same
        assertEquals(expectedMapPins, mapPins);

        // ---> test passed successfully.
    }*/
}