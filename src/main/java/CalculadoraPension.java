import java.time.LocalDate;
import java.time.Period;
import java.util.function.Function;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.keymap.KeyMap;
import org.jline.utils.InfoCmp;

public class CalculadoraPension {

    private static final Function<LocalDate, Integer> calcularEdad = fechaNacimiento ->
            Period.between(fechaNacimiento, LocalDate.now()).getYears();

    private static final Function<LocalDate, LocalDate> calcularFechaInicioLaboral = fechaNacimiento ->
            fechaNacimiento.plusYears(18);

    private static final Function<LocalDate, Integer> calcularSemanasCotizadas = fechaInicioLaboral -> {
        Period periodo = Period.between(fechaInicioLaboral, LocalDate.now());
        int totalDias = periodo.getYears() * 365 + periodo.getMonths() * 30 + periodo.getDays();
        return totalDias / 7;
    };

    private static final Function<String, Integer> obtenerEdadPension = genero -> {
        return switch (genero.toLowerCase()) {
            case "hombre" -> 62;
            case "mujer" -> 57;
            default -> throw new IllegalArgumentException("Género no válido");
        };
    };

    private static final Function<String, Integer> obtenerSemanasMinimas = genero -> {
        int year = LocalDate.now().getYear();
        if (genero.equalsIgnoreCase("hombre")) {
            return 1300;
        } else if (genero.equalsIgnoreCase("mujer")) {
            int semanas = 1275 - ((year - 2025) * 25);
            return Math.max(semanas, 1000);
        } else {
            throw new IllegalArgumentException("Género no válido");
        }
    };

    public static void calcularPension(LocalDate fechaNacimiento, String genero) {
        int edadActual = calcularEdad.apply(fechaNacimiento);
        LocalDate fechaInicioLaboral = calcularFechaInicioLaboral.apply(fechaNacimiento);
        int semanasCotizadas = calcularSemanasCotizadas.apply(fechaInicioLaboral);
        int edadPension = obtenerEdadPension.apply(genero);
        int semanasMinimas = obtenerSemanasMinimas.apply(genero);

        LocalDate fechaPension = fechaNacimiento.plusYears(edadPension);
        boolean cumpleRequisitos = edadActual >= edadPension && semanasCotizadas >= semanasMinimas;

        System.out.println("\n\ud83d\udccc Resumen de tu situación de pensión:");
        System.out.println("\ud83d\udc49 Semanas cotizadas: " + semanasCotizadas);
        System.out.println("\ud83d\udcc5 Fecha estimada en la que podrías pensionarte: " + fechaPension);
        System.out.println("\u2705 ¿Cumples con los requisitos para pensionarte? " + (cumpleRequisitos ? "Sí \ud83c\udf89" : "No \u274c"));
    }

    public static int mostrarMenuOpciones(Terminal terminal, String[] opciones) throws Exception {
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).history(new DefaultHistory()).build();
        KeyMap<String> keys = new KeyMap<>();
        keys.bind("prev", "\033[A"); // Arrow Up
        keys.bind("next", "\033[B"); // Arrow Down
        keys.bind("select", "\r"); // Enter Key

        int seleccion = 0;
        while (true) {
            System.out.println("\n\u2696 Selecciona tu género:");
            for (int i = 0; i < opciones.length; i++) {
                System.out.println((i == seleccion ? "\u27a1 " : "   ") + opciones[i]);
            }
            String key = reader.readLine("");
            if (key.equals("prev")) {
                seleccion = (seleccion - 1 + opciones.length) % opciones.length;
            } else if (key.equals("next")) {
                seleccion = (seleccion + 1) % opciones.length;
            } else if (key.equals("select")) {
                break;
            }
        }
        return seleccion + 1;
    }

    public static void main(String[] args) throws Exception {
        Terminal terminal = TerminalBuilder.builder().jna(true).build();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

        while (true) {
            System.out.println("\ud83d\udee0 Bienvenido al sistema de cálculo de pensión en Colombia \ud83d\udee0");
            System.out.println("Por favor, ingresa tu información a continuación:");

            System.out.print("\ud83d\udcc6 Ingresa tu año de nacimiento (1950 - " + LocalDate.now().getYear() + "): ");
            int year = Integer.parseInt(reader.readLine());

            System.out.print("\ud83d\udcc6 Ingresa tu mes de nacimiento (1 - 12): ");
            int month = Integer.parseInt(reader.readLine());

            System.out.print("\ud83d\udcc6 Ingresa tu día de nacimiento (1 - 31): ");
            int day = Integer.parseInt(reader.readLine());

            String[] opcionesGenero = {"Hombre", "Mujer"};
            int opcionGenero = mostrarMenuOpciones(terminal, opcionesGenero);
            String genero = (opcionGenero == 1) ? "hombre" : "mujer";

            LocalDate fechaNacimiento = LocalDate.of(year, month, day);
            calcularPension(fechaNacimiento, genero);

            System.out.println("\n\ud83d\udd04 ¿Deseas realizar otro cálculo? (s/n): ");
            String continuar = reader.readLine().trim().toLowerCase();
            if (!continuar.equals("s")) {
                System.out.println("\u2728 Gracias por usar nuestro sistema. ¡Que tengas un excelente día! \u2728");
                break;
            }
        }
    }
}
