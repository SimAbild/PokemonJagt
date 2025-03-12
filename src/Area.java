import java.util.ArrayList;

public class Area {
    private ArrayList<Pokemon> pokemonList = new ArrayList<>();

    public void addPokemon(Pokemon pokemon) {
        pokemonList.add(pokemon);
    }

    public void showAllPokemon() {
        for (Pokemon p : pokemonList) {
            System.out.println(p);
        }
    }

    public Pokemon searchPokemonByName(String name) {
        for (Pokemon p : pokemonList) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    public Pokemon searchPokemonByType(String type) {
        for (Pokemon p : pokemonList) {
            if (p.getType().equalsIgnoreCase(type)) {
                return p;
            }
        }
        return null;
    }

    // **Ny metode for at vise alle Pokémoner fra to områder**
    public static void showAllPokemonFromAreas(Area area1, Area area2) {
        System.out.println("\nAlle Pokémoner fra begge områder:");
        area1.showAllPokemon();
        area2.showAllPokemon();
    }
}