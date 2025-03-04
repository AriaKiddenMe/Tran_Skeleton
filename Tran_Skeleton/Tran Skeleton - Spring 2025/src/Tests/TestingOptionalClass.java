package Tests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.Optional;

public class TestingOptionalClass {

    @Test
    public void optionalRemovesOrPeeks() throws Exception{
        Optional<String> optionalToken = Optional.of("spooky");
        String string = optionalToken.get();
        Assertions.assertTrue(optionalToken.isPresent());
    }

    public void testedAndGood(Optional<String> optionalS){
        if(optionalS.isPresent()){
            String sucker = optionalS.get();
        }
    }
}
