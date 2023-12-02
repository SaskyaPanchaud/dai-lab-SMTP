package dai.smtp.client;

import java.util.Arrays;
import java.util.Random;
import java.util.ArrayList;

public class Util {
    static Object[] getRandomElements(Object[] elements, int minAmount, int maxAmount) {
        if (elements.length < minAmount) {
            throw new RuntimeException("There are not enough elements to select in the input array");
        }
        final Random RAND = new Random();
        // Min ensures that the random amount of elements is not greater than the total length of the elements.
        int nElements = Math.min(RAND.nextInt(maxAmount - minAmount + 1) + minAmount, elements.length);
        Object[] selectedElements = new Object[nElements];
        ArrayList<Object> mutableElements = new ArrayList<Object>(Arrays.asList(elements));

        for (int i = 0; i < nElements; ++i) {
            var idx = RAND.nextInt(mutableElements.size());
            selectedElements[i] = mutableElements.get(idx);;
            mutableElements.remove(idx);
        }

        return selectedElements;
    }
}
