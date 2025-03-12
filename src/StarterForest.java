import java.util.ArrayList;

public class StarterForest {
        private ArrayList<Pokemon> pokemonListStarterForest = new ArrayList<>();

        public void addPokemon(Pokemon pokemon) {
            pokemonListStarterForest.add(pokemon);
        }

        public void showAllPokemon() {
            System.out.println("\nPokémon i skoven:");
            for (Pokemon p : pokemonListStarterForest) {
                System.out.println(p);
            }
        }

        public Pokemon searchPokemonByName(String name) {
            for (Pokemon p : pokemonListStarterForest) {
                if (p.getName().equalsIgnoreCase(name)) {
                    return p;
                }
            }
            return null;
        }

        public Pokemon searchPokemonByType(String type) {
            for (Pokemon p : pokemonListStarterForest) {
                if (p.getType().equalsIgnoreCase(type)) {
                    return p;
                }
            }
            return null;
        }
    }