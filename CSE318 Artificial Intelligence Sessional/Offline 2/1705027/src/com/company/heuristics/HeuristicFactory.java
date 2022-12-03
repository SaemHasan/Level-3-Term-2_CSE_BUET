package com.company.heuristics;

public class HeuristicFactory {

    public HeuristicFactory() {
    }

    public Heuristic getHeuristicFromFactory(int choice){
        if(choice == 0){
            return new HumanHeuristic();
        }
        else if(choice == 1){
            return new Heuristic1();
        }
        else if(choice == 2){
            return new Heuristic2();
        }
        else if(choice == 3){
            return new Heuristic3();
        }
        else if(choice == 4){
            return new Heuristic4();
        }
        else if(choice == 5){
            return new Heuristic5();
        }else if(choice == 6){
            return new Heuristic6();
        }

        return new Heuristic1();
    }
}
