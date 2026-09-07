
import org.junit.Test;
import org.junit.Assert;

import java.math.BigInteger;
public class Evaluate {
    @Test(timeout = 50000)
    public void testExercise() throws Exception{
        ComplexCaculation complexCalculation = new ComplexCaculation();
        BigInteger base1 = new BigInteger("3");
        BigInteger power1 = new BigInteger("10");
        BigInteger base2 = new BigInteger("2");
        BigInteger power2 = new BigInteger("6");
        BigInteger expectedResult = base1.pow(power1.intValue()).add(base2.pow(power2.intValue()));
        BigInteger actualResult = complexCalculation.calculateResult(base1, power1, base2, power2);
        Assert.assertEquals("The result of the calculation is incorrect", expectedResult,  actualResult);
    }
}