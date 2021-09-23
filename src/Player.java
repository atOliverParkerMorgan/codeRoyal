import java.util.*;
import java.io.*;
import java.math.*;
class Player {
    static int hideX = -1;
    static int hideY = -1;

    static final int X = 0;
    static final int Y = 1;
    static final int RADIUS = 2;
    static final int GOLD = 3;
    static final int MINE_SIZE = 4;
    static final int STRUCTURE_TYPE = 5;
    static final int OWNER = 6;
    static final int PARAM1 = 7;
    static final int PARAM2 = 8;

    static final int NONE = -1;
    static final int FRIENDLY = 0;
    static final int ENEMY = 1;

    static final int QUEEN = -1;
    static final int KNIGHT = 0;
    static final int ARCHER = 1;
    static final int GIANT = 2;

    static final int MINE = 0;
    static final int TOWER = 1;
    static final int BARRACKS = 2;
    static final int BARRACK_TYPE = -3;

    static class Unit{
        Unit(int x, int y, int owner, int unitType, int health){
            this.x =x;
            this.y = y;
            this.owner = owner;
            this.unitType = unitType;
            this.health = health;

        }
        int x;
        int y;
        int owner;
        int unitType; // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER
        int health;
        


    }

    static int currentStructureId = -1;
    public static boolean contains(int i , int[] array){
        for (int j : array) {
            if(j==i) return true;
        }
        return false;

    }
    public static Unit getUnit(Unit[] allU, int owner, int unitType){
        for (Unit unit : allU) {
            if(unit.owner==owner&&unit.unitType==unitType) return unit;
        }
        return null;
    }
    public static int getDistance(int x1, int y1, int x2, int y2){
        return (int)(Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2,2)));
    }
    public static int[] getClosestUnitToUnit(Unit[] allU, int UnitType1, int UnitOwner1, int UnitType2, int UnitOwner2){

        int[] minDistance = new int[]{9999, -1,-1};
        for (Unit unit : allU) {
            Unit goalUnit = getUnit(allU, UnitOwner1, UnitType1);

            int distance = getDistance(unit.x, unit.y, goalUnit.x, goalUnit.y);

            if(distance<minDistance[0]&&unit.owner==UnitOwner2&&unit.unitType==UnitType2){
                minDistance[0]=distance;
                minDistance[1]=unit.x;
                minDistance[2]=unit.y;
            
            }
        }
            return new int[]{minDistance[0], minDistance[1],minDistance[2]};
    }
    
    static final int STRUCTURE_ID = 1;
    static final int DISTANCE = 0;
    public static int[] getClosestStructureToUnit(Map<Integer, int[]> Structures, Unit[] allU, int owner, int unitType, int[] goalStructureOwner, int[] goalStructureType){ 
        int[] minDistance = new int[]{9999, -1};
        for (Map.Entry<Integer, int[]> entry : Structures.entrySet()) {
            int[] structure = entry.getValue();
            
            Unit unit = getUnit(allU, owner, unitType);

            int distance = getDistance(unit.x, unit.y, structure[X], structure[Y]);

            boolean structureType = contains(structure[STRUCTURE_TYPE], goalStructureType);
            if(contains(BARRACK_TYPE, goalStructureType)){
                structureType = contains(structure[PARAM2], goalStructureType);
            }

            if(distance<minDistance[DISTANCE]&&contains(structure[OWNER],goalStructureOwner)&&structureType){
                minDistance[DISTANCE]=distance;
                minDistance[STRUCTURE_ID]=entry.getKey();
            }
        }
            return new int[]{ minDistance[DISTANCE], minDistance[STRUCTURE_ID]};
    }
    public static float get_0_to_100(int max, int min, int value, float retrunIfSmallerThenZero){
        float range = max - min;

        if(value>max) return 100;
        if(value<=retrunIfSmallerThenZero||range<=0) return retrunIfSmallerThenZero;
        return (float)(value/range)*100;
    }
   
    public static int getNumberOfEnemyKnightsAttacking(Unit[] allU){
        int total = 0;
        for (Unit unit : allU) {
            if(unit.owner==ENEMY&&unit.unitType==KNIGHT) total++;
        }
        return total;
    
    }
    public static int getEnemyKnightTotal_0_100(Unit[] allU){
        int total = 0;
        for (Unit unit : allU) {
            if(unit.owner==ENEMY&&unit.unitType==KNIGHT)total++;
        }
        return  total;
    
    }
    public static int getDistanceToMoves(int distance, int moveDist){
        return distance/moveDist;

    }

    public static int getPossitionValue(int x, int y, Map<Integer, int[]> Structures, boolean isEnemyBarrack, int[] enemyBarrack, Unit queen){
        int value = 0;

        if(isEnemyBarrack){
            value += 4*getDistanceToMoves(getDistance(x, y, enemyBarrack[X], enemyBarrack[Y]),60);
        }

        for (Map.Entry<Integer, int[]> s : Structures.entrySet()) {
            int[] structure = s.getValue();
            int dist = getDistance(x,y, structure[X], structure[Y]);
            if(structure[STRUCTURE_TYPE]==TOWER&&dist<=structure[PARAM2]){
                if(structure[OWNER]==FRIENDLY) value+=1500/(queen.health*0.01);
                else if(structure[OWNER]==ENEMY) System.err.println("ENEMY TOWER"); value-=6000/(queen.health*0.01);
            }
        }

        return value;
    }

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int numSites = in.nextInt();
        Map<Integer, int[]> Structures = new HashMap<>();
        
        Map<Integer, Integer> UnitsType = new HashMap<>();
        Map<Integer, int[]> Units = new HashMap<>();

        for (int i = 0; i < numSites; i++) {
            int siteId = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            int radius = in.nextInt();
            Structures.put(siteId,new int[]{x,y,radius,-2,-2,-2,-2,-2,-2});
        }

        boolean hidePostion = true;
        // game loop
        while (true) {
            int enemyTowersNum = 0;
            int friendlyTowersNum = 0;
            int friendlyMineNum = 0;
            int enemyMineNum = 0;
            double numberKnightBarracks = 1;
            double numberArcherBarracks = 1;
            int numberEnemyKnightBarracks = 0;
            int[] enemyKnightBarrackData = new int[]{hideX, hideY};
            int numberGiantBarracks = 1;
            int unClaimedStructers = 0;
            int goldIntake = 0;
            
            int gold = in.nextInt();
            int touchedSite = in.nextInt(); // -1 if none
            for (int i = 0; i < numSites; i++) {
                int siteId = in.nextInt();
                for (int j = 3; j < 9 ; j++) {
                    Structures.get(siteId)[j] = in.nextInt();
                }
                if(Structures.get(siteId)[5]==-1) unClaimedStructers++;
                else if(Structures.get(siteId)[STRUCTURE_TYPE]==TOWER && Structures.get(siteId)[OWNER]==ENEMY) enemyTowersNum++;
                else if(Structures.get(siteId)[STRUCTURE_TYPE]==TOWER && Structures.get(siteId)[OWNER]==FRIENDLY) friendlyTowersNum++;
                else if(Structures.get(siteId)[STRUCTURE_TYPE]==MINE && Structures.get(siteId)[OWNER]==FRIENDLY) friendlyMineNum++;
                else if(Structures.get(siteId)[STRUCTURE_TYPE]==MINE && Structures.get(siteId)[OWNER]==ENEMY) enemyMineNum++;
                else if(Structures.get(siteId)[PARAM2]==KNIGHT && Structures.get(siteId)[OWNER]==FRIENDLY){ 
                    numberKnightBarracks++;
                    enemyKnightBarrackData=Structures.get(siteId);
                }
                else if(Structures.get(siteId)[PARAM2]==ARCHER && Structures.get(siteId)[OWNER]==FRIENDLY){ 
                    numberArcherBarracks++;
                }
                else if(Structures.get(siteId)[PARAM2]==KNIGHT && Structures.get(siteId)[OWNER]==ENEMY) numberEnemyKnightBarracks++;
                else if(Structures.get(siteId)[PARAM2]==GIANT && Structures.get(siteId)[OWNER]==FRIENDLY) numberGiantBarracks++;
                else if(Structures.get(siteId)[STRUCTURE_TYPE]==MINE && Structures.get(siteId)[OWNER]==FRIENDLY) goldIntake+=Structures.get(siteId)[4];
             

            }
            int numUnits = in.nextInt();
            Unit[] allU = new Unit[numUnits];
            for (int i = 0; i < numUnits; i++) {
                allU[i] = new Unit(in.nextInt(),in.nextInt(),in.nextInt(),in.nextInt(),in.nextInt());
            }
            if(hidePostion){
                hidePostion = false;
                Unit Queen = getUnit(allU, 0, -1);
                hideX = Queen.x;
                hideY = Queen.y;

                // input begginngGold In each site
            }
            Unit queen = getUnit(allU, FRIENDLY, QUEEN);
            Unit enemyQueen = getUnit(allU, ENEMY, QUEEN);
            
            int[] closestKnightBarrackToEnemyQueen = getClosestStructureToUnit(Structures, allU, ENEMY, QUEEN, new int[]{FRIENDLY}, new int[]{BARRACK_TYPE,KNIGHT});
            int[] closestGiantBarrackToEnemyQueen = getClosestStructureToUnit(Structures, allU, ENEMY, QUEEN, new int[]{FRIENDLY}, new int[]{BARRACK_TYPE,GIANT});
            int[] closestArcherBarrackToEnemyQueen = getClosestStructureToUnit(Structures, allU, FRIENDLY, QUEEN, new int[]{FRIENDLY}, new int[]{BARRACK_TYPE,ARCHER});
            
            // rank all closest structures
            int[][][] allStructureDistances = new int[Structures.size()][2][2];
            int index = 0;
            for (Map.Entry<Integer, int[]> entry : Structures.entrySet()) {
                HashMap<Integer, int[]> e = new HashMap<>();
                e.put(entry.getKey(), entry.getValue());
                allStructureDistances[index][0] = getClosestStructureToUnit(e, allU, FRIENDLY, QUEEN, new int[]{NONE,FRIENDLY,ENEMY},new int[]{NONE,BARRACKS,TOWER,MINE});
                allStructureDistances[index][1] = getClosestStructureToUnit(e, allU, ENEMY, QUEEN, new int[]{NONE,FRIENDLY,ENEMY},new int[]{NONE,BARRACKS,TOWER,MINE});
                index++;
            }
            float maxValue = Integer.MIN_VALUE;
            String output = "WAIT";
            
            float mineDebug = -1;
            float towerDebug = -1;;
            float barrackKnightDebug = -1;
            float barrackArcherDebug = -1;
            float barrackGiantDebug = -1;
            float hideDebug = -1;

            int EnemyKnightTotal = getEnemyKnightTotal_0_100(allU);
            
            for (int[][] structure : allStructureDistances) { 

                int[] structureToQueenIdData = Structures.get(structure[0][STRUCTURE_ID]);
                float distanceToQueen = structure[0][DISTANCE];

                int[] structureToEnemyQueenIdData = Structures.get(structure[1][STRUCTURE_ID]);
                float distanceToEnemyQueen = structure[1][DISTANCE];

                boolean checkClosestQueen = true;
                boolean checkFurthestQueen = true;
                if(structureToQueenIdData==null) checkClosestQueen=false;
                if(structureToEnemyQueenIdData==null) checkFurthestQueen=false;

                float closestEnemyUnitDistance_0_100 = 100;
                float distanceToAllMines = 100;
                if(getUnit(allU, ENEMY, KNIGHT)!=null){
                    HashMap<Integer,int[]> h = new HashMap<Integer,int[]>();
                    h.put(structure[0][STRUCTURE_ID], structureToQueenIdData);
                    int closestEnemyUnitDistance = getClosestUnitToUnit( allU, QUEEN, FRIENDLY, KNIGHT, ENEMY)[0];
                    closestEnemyUnitDistance_0_100 = get_0_to_100(2100, 0, closestEnemyUnitDistance, 1);
                }
               

            
                int isEnemy = structureToQueenIdData[OWNER]==ENEMY?-1:1;
                boolean isGoldMine = structureToQueenIdData[STRUCTURE_TYPE]==MINE;
                boolean isGoldMineIsMaxedOut = isGoldMine&&structureToQueenIdData[MINE_SIZE]==structureToQueenIdData[PARAM1];

                boolean isTower = structureToQueenIdData[STRUCTURE_TYPE]==TOWER;
                boolean isTowerMaxedOut = isTower&&structureToQueenIdData[PARAM1]>=700;

                boolean isKnightBarrack = structureToQueenIdData[STRUCTURE_TYPE]==BARRACKS&&structureToQueenIdData[PARAM2]==KNIGHT;
                int numberOfTurnsUntilTrainedKnight = isKnightBarrack?structureToQueenIdData[PARAM1]:1;

                boolean isGiantBarrack = structureToQueenIdData[STRUCTURE_TYPE]==BARRACKS&&structureToQueenIdData[PARAM2]==GIANT;
                int numberOfTurnsUntilTrainedGiant = isGiantBarrack?structureToQueenIdData[PARAM1]:1;

                boolean isArcherBarrack = structureToQueenIdData[STRUCTURE_TYPE]==BARRACKS&&structureToQueenIdData[PARAM2]==ARCHER;
                int numberOfTurnsUntilTrainedArcher = isArcherBarrack?structureToQueenIdData[PARAM1]:1;


                // get values (0;100>
                float mineSize_0_100 = get_0_to_100(5, 1, structureToQueenIdData[MINE_SIZE], 0);
                float goldToMine_0_100 = structureToQueenIdData[GOLD]==-1?0:structureToQueenIdData[GOLD];
               
                int movesToStructureForQueen = getDistanceToMoves(Math.round(distanceToQueen)-30, 60);
                int movesToStructureToEnemyQueen = getDistanceToMoves(Math.round(distanceToEnemyQueen)-30, 60);

                float goldIntake_0_100 = goldIntake<=0?0.01f:goldIntake;
                float miningRate = isGoldMine?structureToQueenIdData[PARAM1]:0;
           
            
                float numberOfGiantBarracks_0_100 = get_0_to_100(1, 0, numberGiantBarracks, 0.01f);
       
                int towerHealth = structureToQueenIdData[STRUCTURE_TYPE]==TOWER?structureToQueenIdData[PARAM1]:1;
  
                int[] distanceOfEnemyBarrack = getClosestStructureToUnit(Structures, allU, FRIENDLY, QUEEN, new int[]{ENEMY}, new int[]{BARRACKS});
                float movesFromEnemyBarrack = getDistanceToMoves(Math.round(distanceOfEnemyBarrack[DISTANCE])-20, 100);

                float position = getPossitionValue(structureToQueenIdData[X], structureToQueenIdData[Y], Structures, numberEnemyKnightBarracks>=1, enemyKnightBarrackData, queen);
               
                float tower = (10*EnemyKnightTotal*movesFromEnemyBarrack)/(movesToStructureForQueen+towerHealth/50)+position;
                float mine = (float)(goldToMine_0_100 + mineSize_0_100+100*numberKnightBarracks)/(((goldIntake_0_100-miningRate))/5+movesToStructureForQueen)+position;
                
               
                
                float barrackKnight = (float)((goldIntake_0_100*10 + gold*7-5*movesToStructureToEnemyQueen-30*movesToStructureForQueen)/(Math.pow(numberKnightBarracks, numberKnightBarracks))+position);//(goldIntake_0_100*gold_0_100+400/distanceToQueen_0_100+200/distanceToEnemyQueen_0_100)/numberOfBarracks_0_100;
                float barrackGiant =  (float)((goldIntake_0_100 + gold*6+10*enemyTowersNum-30*movesToStructureForQueen)/(Math.pow(numberGiantBarracks, numberGiantBarracks))+position);
                float barrackArcher = (float)((goldIntake_0_100*5+ EnemyKnightTotal*10 + gold*4+600/queen.health-30*movesToStructureForQueen)/(Math.pow(numberArcherBarracks, numberArcherBarracks))+position);


                if(isTower){ 
                    isEnemy = isEnemy==-1?15:1;
                    tower = tower<0?0:tower;

                    System.err.println(tower*isEnemy);
                    mine -=tower*isEnemy;
                    barrackKnight -= tower*isEnemy;
                    barrackGiant -= tower*isEnemy;
                    barrackArcher -= tower*isEnemy;
                
                }
                else if(isGoldMine){ 
                    tower-=mine*isEnemy;
                    barrackKnight -= mine*isEnemy;
                    barrackGiant -= mine*isEnemy;
                    barrackArcher -= mine*isEnemy;
                }
                else if(isKnightBarrack){
                    numberOfTurnsUntilTrainedKnight = isEnemy==-1? 0: numberOfTurnsUntilTrainedKnight;
                    mine -= 4*barrackKnight*isEnemy+numberOfTurnsUntilTrainedKnight*150;
                    tower -= 4*barrackKnight*isEnemy*numberOfTurnsUntilTrainedKnight*150;
                    barrackGiant -= 4*barrackKnight*isEnemy+numberOfTurnsUntilTrainedKnight*150;
                    barrackArcher -=4*barrackKnight*isEnemy+numberOfTurnsUntilTrainedKnight*150;
                }

                else if(isGiantBarrack){
                    numberOfTurnsUntilTrainedGiant = isEnemy==-1? 0: numberOfTurnsUntilTrainedGiant;
                    mine -= 4*barrackGiant*isEnemy+numberOfTurnsUntilTrainedGiant*150;
                    tower -= 4*barrackGiant*isEnemy*numberOfTurnsUntilTrainedGiant*150;
                    barrackKnight -= 4*barrackGiant*isEnemy+numberOfTurnsUntilTrainedGiant*150;
                    barrackArcher -=4*barrackGiant*isEnemy+numberOfTurnsUntilTrainedGiant*150;
                }

                else if(isArcherBarrack){
                    numberOfTurnsUntilTrainedArcher = isEnemy==-1? 0: numberOfTurnsUntilTrainedArcher;
                    mine -= 4*barrackArcher*isEnemy+numberOfTurnsUntilTrainedArcher*150;
                    tower -= 4*barrackArcher*isEnemy*numberOfTurnsUntilTrainedArcher*150;
                    barrackKnight -= 4*barrackArcher*isEnemy+numberOfTurnsUntilTrainedArcher*150;
                    barrackGiant -=4*barrackArcher*isEnemy+numberOfTurnsUntilTrainedArcher*150;
                }

                if(isTowerMaxedOut) tower = Integer.MIN_VALUE;
                if(isGoldMineIsMaxedOut) mine = Integer.MIN_VALUE;


                float distanceToHidingPlace = 0;//(int)(Math.sqrt(Math.pow(hideX-queen.x, 2)+Math.pow(hideY-queen.y,2)));
                distanceToHidingPlace = get_0_to_100(2100,0, Math.round(distanceToHidingPlace),1);

                float hide = 0;//((gold_0_100)*goldIntake_0_100)/(enemyQueen.health+queen.health);

                if(mine>maxValue&&checkClosestQueen){
                    maxValue=mine;
                    mineDebug = mine;
                    output = "BUILD "+structure[0][STRUCTURE_ID]+" MINE";

                }if(tower>maxValue&&checkClosestQueen){
                    maxValue=tower;
                    towerDebug = tower;
                    output = "BUILD "+structure[0][STRUCTURE_ID]+" TOWER";

                }if(barrackKnight>maxValue&&checkFurthestQueen){
                    maxValue=barrackKnight;
                    barrackKnightDebug = barrackKnight;
                    output = "BUILD "+structure[1][STRUCTURE_ID]+" BARRACKS-KNIGHT";

                }if(barrackGiant>maxValue&&checkFurthestQueen){
                    maxValue=barrackGiant;
                    output = "BUILD "+structure[1][STRUCTURE_ID]+" BARRACKS-GIANT";
                    barrackGiantDebug = barrackGiant;
                }if(barrackArcher>maxValue&&checkFurthestQueen){
                    maxValue=barrackArcher;
                    output = "BUILD "+structure[1][STRUCTURE_ID]+" BARRACKS-ARCHER";
                    barrackArcherDebug = barrackGiant;
                }if(hide>maxValue){
                    maxValue=hide;
                    hideDebug = hide;
                }
            }

            System.err.println("Position value: "+getPossitionValue(queen.x, queen.y, Structures, numberEnemyKnightBarracks>=1, enemyKnightBarrackData, queen));
            System.err.println("MINE: "+mineDebug);
            System.err.println("TOWER: "+towerDebug);
            System.err.println("BARRACK-KNIGHT: "+barrackKnightDebug);
            System.err.println("BARRACK-GIANT: "+barrackGiantDebug); 
            System.err.println("BARRACK-ARCHER: "+barrackArcherDebug);
            System.err.println("MOVE: "+hideDebug);
            System.err.print("CHOSEN: "+output+";  MAXVALUE: "+maxValue);

            System.out.println(output);

          

            String trainSiteId = " "; 
            if(friendlyTowersNum<enemyTowersNum&&closestGiantBarrackToEnemyQueen[STRUCTURE_ID]!=NONE){ 
                trainSiteId += String.valueOf(closestGiantBarrackToEnemyQueen[STRUCTURE_ID])+" ";
                
                
            }
            if(closestArcherBarrackToEnemyQueen[STRUCTURE_ID]!=NONE&&EnemyKnightTotal>5){ 
                trainSiteId += String.valueOf(closestKnightBarrackToEnemyQueen[STRUCTURE_ID])+" ";
                
                
            }
            if(closestKnightBarrackToEnemyQueen[STRUCTURE_ID]!=NONE&&gold>=80&&(2*friendlyTowersNum>=enemyTowersNum||enemyMineNum*2>enemyTowersNum)){ 
                trainSiteId += String.valueOf(closestKnightBarrackToEnemyQueen[STRUCTURE_ID])+" ";
            }

         
            System.out.println("TRAIN"+trainSiteId.substring(0, trainSiteId.length()-1));
            
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");


            // First line: A valid queen action
            // Second line: A set of training instructions
//            System.out.println("WAIT");
//            System.out.println("TRAIN");
        }
    }
}