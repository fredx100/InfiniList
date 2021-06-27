package uk.sensoryunderload.infinilist;

import org.junit.Test;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UpdateWidgetAddressTest {
    @Test
    public void UpdateTest() {
        ArrayList<Integer> addressMaster = new ArrayList<Integer>();
        ArrayList<Integer> changedAddress = new ArrayList<Integer>();
        ArrayList<Integer> address;
        ArrayList<Integer> expectedResult;

        addressMaster.add(3);
        addressMaster.add(0);
        addressMaster.add(7);
        addressMaster.add(6);
        addressMaster.add(1);

        // List has changed
        changedAddress = new ArrayList<Integer>(addressMaster);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, 2, 4)); // Move
        assertEquals(address, addressMaster);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, -1, 4)); // Delete
        assertEquals(address, addressMaster);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, 2, -1)); // Insertion
        assertEquals(address, addressMaster);


        // No addressIntersect
        changedAddress.set(2, 6);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, 2, 4)); // Move
        assertEquals(address, addressMaster);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, -1, 4)); // Delete
        assertEquals(address, addressMaster);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, 2, -1)); // Insertion
        assertEquals(address, addressMaster);


        // Address intersect
        changedAddress = new ArrayList<Integer>();
        changedAddress.add(3);
        changedAddress.add(0);
        changedAddress.add(7);

        // No change
        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, 2, 3)); // Both before address (balances out)
        assertEquals(address, addressMaster);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, 7, 8)); // After end of address
        assertEquals(address, addressMaster);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, 7, -1)); // Insertion
        assertEquals(address, addressMaster);

        address = new ArrayList<Integer>(addressMaster);
        assertFalse(ListView.updateWidgetAddress (address, changedAddress, -1, 8)); // Deletion
        assertEquals(address, addressMaster);

        // Changed
        address = new ArrayList<Integer>(addressMaster);
        expectedResult = new ArrayList<Integer>(addressMaster);
        expectedResult.set(3, 7);
        assertTrue(ListView.updateWidgetAddress (address, changedAddress, 2, 8)); // Move
        assertEquals(address, expectedResult);

        address = new ArrayList<Integer>(addressMaster);
        expectedResult.set(3, 5);
        assertTrue(ListView.updateWidgetAddress (address, changedAddress, 7, 3)); // Move
        assertEquals(address, expectedResult);

        address = new ArrayList<Integer>(addressMaster);
        expectedResult.set(3, 7);
        assertTrue(ListView.updateWidgetAddress (address, changedAddress, 5, -1)); // Insertion
        assertEquals(address, expectedResult);

        address = new ArrayList<Integer>(addressMaster);
        expectedResult.set(3, 5);
        assertTrue(ListView.updateWidgetAddress (address, changedAddress, -1, 3)); // Deletion
        assertEquals(address, expectedResult);

        // Deleted
        address = new ArrayList<Integer>(addressMaster);
        expectedResult = new ArrayList<Integer>();
        assertTrue(ListView.updateWidgetAddress (address, changedAddress, -1, 6)); // Deletion of parent list
        assertEquals(address, expectedResult);

        changedAddress = new ArrayList<Integer>();
        changedAddress.add(3);
        changedAddress.add(0);
        changedAddress.add(7);
        changedAddress.add(6);
        address = new ArrayList<Integer>(addressMaster);
        expectedResult = new ArrayList<Integer>();
        assertTrue(ListView.updateWidgetAddress (address, changedAddress, -1, 1)); // Deletion of list
        assertEquals(address, expectedResult);
    }
}
