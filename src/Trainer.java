public class Trainer {
    private String name;

    public Trainer(String name) {
        this.name = name;
    }

    public void searchForPokemon(Forest forest, String searchType, String value) {
        Pokemon foundPokemon = null;

        if (searchType.equalsIgnoreCase("name")) {
            foundPokemon = forest.searchPokemonByName(value);
        }

        if (searchType.equalsIgnoreCase("type")) {
            foundPokemon = forest.searchPokemonByType(value);
        }

        if (foundPokemon != null) {
            System.out.println(name + " fandt en " + foundPokemon);
        } else {
            System.out.println(name + " kunne ikke finde en Pokémon med " + searchType + " '" + value + "'.");
        }
    }

    public void searchForPokemon(StarterForest starterForest, String searchType, String value) {
        Pokemon foundPokemon = null;

        if (searchType.equalsIgnoreCase("name")) {
            foundPokemon = starterForest.searchPokemonByName(value);
        }

        if (searchType.equalsIgnoreCase("type")) {
            foundPokemon = starterForest.searchPokemonByType(value);
        }

        if (foundPokemon != null) {
            System.out.println(name + " fandt en " + foundPokemon);
        } else {
            System.out.println(name + " kunne ikke finde en Pokémon med " + searchType + " '" + value + "'.");
        }
    }
}
