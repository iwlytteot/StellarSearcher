import cz.muni.fi.tovarys.model.Coordinates;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CoordinatesTest {
    @Test
    public void createPositive() {
        var coordinatesNoSign = new Coordinates("1.2868356", "67.83999458");

        assertThat(coordinatesNoSign.getRa()).isEqualTo(1.2868356);
        assertThat(coordinatesNoSign.getDec()).isEqualTo(67.83999458);

        var coordinatesWithSign = new Coordinates("1.2868356", "+67.83999458");

        assertThat(coordinatesWithSign.getRa()).isEqualTo(1.2868356);
        assertThat(coordinatesWithSign.getDec()).isEqualTo(67.83999458);
    }

    @Test
    public void createNegative() {
        var coordinates = new Coordinates("1.2868356", "-67.83999458");

        assertThat(coordinates.getRa()).isEqualTo(1.2868356);
        assertThat(coordinates.getDec()).isEqualTo(-67.83999458);
    }

    @Test
    public void compareEqualsTest() {
        var coordinatesNoSign = new Coordinates("1.2868356", "67.83999458");
        var coordinatesWithSign = new Coordinates("1.2868356", "+67.83999458");

        assertThat(coordinatesNoSign).isEqualTo(coordinatesWithSign);
    }
    @Test
    public void compareNotEqualsTest() {
        var coordinatesNoSign = new Coordinates("1.2868356", "67.83999458");
        var coordinatesWithSign = new Coordinates("1.2868356", "-67.83999458");

        assertThat(coordinatesNoSign).isNotEqualTo(coordinatesWithSign);
    }
    @Test
    public void increaseRa() {
        var coordinates = new Coordinates("1.2868356", "67.83999458");

        coordinates.offsetRa(47);
        assertThat(coordinates.getRa()).isEqualTo(1.2868356 + 47);

        coordinates.offsetRa(325);
        assertThat(coordinates.getRa()).isEqualTo(360);
    }

    @Test
    public void decreaseRa() {
        var coordinates = new Coordinates("174.2868356", "67.83999458");

        coordinates.offsetRa(-52);
        assertThat(coordinates.getRa()).isEqualTo(174.2868356 - 52);

        coordinates.offsetRa(-704);
        assertThat(coordinates.getRa()).isEqualTo(0);
    }

    @Test
    public void increaseDec() {
        var coordinates = new Coordinates("174.2868356", "67.83999458");

        coordinates.offsetDec(12);
        assertThat(coordinates.getDec()).isEqualTo(67.83999458 + 12);

        coordinates.offsetDec(30);
        assertThat(coordinates.getDec()).isEqualTo(90);
    }

    @Test
    public void decreaseDec() {
        var coordinates = new Coordinates("174.2868356", "67.83999458");

        coordinates.offsetDec(-20);
        assertThat(coordinates.getDec()).isEqualTo(67.83999458 - 20);

        coordinates.offsetDec(-60);
        assertThat(coordinates.getDec()).isEqualTo(67.83999458 - 20 - 60);

        coordinates.offsetDec(-97);
        assertThat(coordinates.getDec()).isEqualTo(-90);
    }

    @Test
    public void changeSign() {
        var coordinates = new Coordinates("174.2868356", "-67.83999458");

        coordinates.offsetDec(97);
        assertThat(coordinates.getDec()).isEqualTo(-67.83999458 + 97);
    }
}
