package Test;

import SimulationApplication.GridContent.Entity.Human.HumanParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModelBugRegressionTest {

    @Test
    void aZeroEatIntervalIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new HumanParameters(0, 1, 15, 3));
    }

    @Test
    void aZeroBreedIntervalIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new HumanParameters(15, 1, 0, 3));
    }

    @Test
    void aZeroIntervalIsRejectedBySettersToo() {
        HumanParameters parameters = new HumanParameters();
        Assertions.assertThrows(IllegalArgumentException.class, () -> parameters.setEatInterval(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> parameters.setBreedInterval(-1));
    }
}
