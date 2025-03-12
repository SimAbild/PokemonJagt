public class Main {
    public static void main(String[] args) {
        // Opret skoven og tilføj Pokémoner
        Forest forest = new Forest();
        StarterForest starterForest = new StarterForest();
        forest.addPokemon(new Pokemon("Mudkip", "Water", 10));
        forest.addPokemon(new Pokemon("Torchic", "Fire", 12));
        forest.addPokemon(new Pokemon("Treecko", "Grass", 8));
        forest.addPokemon(new Pokemon("Metagross", "Steel", 7));
        forest.addPokemon(new Pokemon("Slaking", "Normal", 12));

        //Lavet tilføjelse til ny område
        starterForest.addPokemon(new Pokemon("Aggron", "Steel", 5));
        starterForest.addPokemon(new Pokemon("Swablu","Flying", 5));
        starterForest.addPokemon(new Pokemon("Eevee", "Normal", 5));
        starterForest.addPokemon(new Pokemon("Jolteon","Electric", 10));



        // Vis alle Pokémoner i skoven
        forest.showAllPokemon();

        //Vis alle Pokemoner i bjerget
        starterForest.showAllPokemon();

        //


        // Opret en træner
        Trainer ash = new Trainer("Ash");

        // Træneren søger efter en Pokémon baseret på navn
        ash.searchForPokemon(forest, "name", "Bulbasaur");

        // Træneren søger efter en Pokémon baseret på type
        ash.searchForPokemon(forest, "type", "Grass");

        // Prøver at søge efter en ikke-eksisterende Pokémon
        ash.searchForPokemon(forest, "name", "Mewtwo");




        System.out.println("\n Vis alle pokemonner");

        // Kalder den nye metode for at vise Pokémoner fra begge områder
        Area.showAllPokemonFromAreas(forest, starterForest);

    }
}
