import java.util.*;
import java.io.*;
import java.math.*;

class Player {

    public static void main(String args[]){
        Scanner in = new Scanner(System.in);
        
        String command = "";
        int canFire = 0;
        // game loop
        while (true) {
			
			ShipHandler handler = new ShipHandler();
            int myShipCount = in.nextInt(); // the number of remaining ships
            int entityCount = in.nextInt(); // the number of entities (e.g. ships, mines or cannonballs)
            
            for (int i = 0; i < entityCount; i++) {
                int entityId = in.nextInt();
                String entityType = in.next();
                int x_ = in.nextInt();
                int y_ = in.nextInt();
                int arg1 = in.nextInt();
                int arg2 = in.nextInt();
                int arg3 = in.nextInt();
                int arg4 = in.nextInt();
                
                if(entityType.equals("BARREL")){
                    Barrel b1 = new Barrel(x_, y_, arg1, entityId);
                    handler.addBarrel(b1);
                }
                if(entityType.equals("SHIP")){
                    Ship s1 = new Ship(x_, y_, arg1, arg2, arg3, arg4, entityId);
                    handler.addShip(s1);
                }
                if(entityType.equals("CANNONBALL")){
                    Ball b = new Ball(x_, y_, arg1, arg2);
                    handler.addBall(b);
                }
            }
            
            
            
            for(Ship s : handler.getShips()) { //for every ship
                if(s.getFoe() == 1) { //if the ship is controlled by us
                    
                    System.out.println(s.action(canFire, handler.getBarrels(),
                                                handler.getShips(),
                                                handler.getMines(),
                                                handler.getBalls()));
                }
                
            }
            command = "";
            
            canFire++;
        }   
    }
}

class ShipHandler {
    private ArrayList<Ship> ships;
    private ArrayList<Barrel> barrels;
    private ArrayList<Mine> mines;
    private ArrayList<Ball> balls;
    private ArrayList<Coordinate> nextMoves;
    
    
    public ShipHandler(){
        ships = new ArrayList<Ship>();
        barrels = new ArrayList<Barrel>();
        mines = new ArrayList<Mine>();
        balls = new ArrayList<Ball>();
        nextMoves = new ArrayList<Coordinate>();
    }
    
    public static int oppositeRotation(int i){
        return (i+3)%6;
    }
    
    public void addShip(Ship s){
        ships.add(s);
    }
    
    public void addBarrel(Barrel b){
        barrels.add(b);
    }
    
    public void addMine(Mine m) {
        mines.add(m);
    }
    
    public void addBall(Ball b){
        balls.add(b);
    }
    
    public ArrayList<Ship> getShips(){
        return ships;
    }
    
    public ArrayList<Barrel> getBarrels(){
        return barrels;
    }
    
    public ArrayList<Mine> getMines() {
        return mines;
    }
    
    public ArrayList<Ball> getBalls() {
        return balls;
    }
}

class Ship {
    private Coordinate coord;
    private int rot;
    private int speed;
    private int rum;
    private int foe;
    private int id;
    int endMove = 0;
    
    public Ship(int _x, int _y, int ro, int s, int r, int f, int i){
        coord = new Coordinate(_x,_y);
        rot = ro;
        speed = s;
        rum = r;
        foe = f;
        id = i;
    }
    
    public Ship(){
        coord = new Coordinate(0,0);
        rot = 0;
        speed = 0;
        rum = 0;
        foe = 0;
        id = 0;
    }
    
    public String action(int frame, ArrayList<Barrel> barrels, ArrayList<Ship> ships, ArrayList<Mine> mines, ArrayList<Ball> balls){
        
        String avoidBalls = avoidBalls(balls, mines);
        if(avoidBalls.length()>1){ return avoidBalls; }
        
        if(frame%3==0){
            
            String fire = fire(ships);
            if(fire.length()>1){ return fire; }
        }
        
        String move = move(barrels, mines, balls);
        if(move.length()>1){ return move; }
        
        return "WAIT";
    }
    
    public String avoidBalls(ArrayList<Ball> balls, ArrayList<Mine> mines){
     //check if a ball will hit our ship
	 ArrayList<Coordinate> t = trajectory();
        for(Ball b: balls){
            int turns = b.getTurnsToImpact();
			if(turns == 1){
				//if center of ship will be hit next turn
				Coordinate check1 = positionAfterTurns(1);
				if(check1.getX()==b.getTargetX() && check1.getY()==b.getTargetY()){
					if(this.speed == 1){
						return "FASTER turn1 center";
					}
					else if(this.speed == 2){
						return "SLOWER turn1 center";
					}
					else{
						return "FASTER turn1 center";
					}
				}
				//if front of ship will be hit next turn
				Coordinate check2 = cMBR(check1, this.rot); //front of ship after one turn 
				if(check2.getX()==b.getTargetX() && check2.getY()==b.getTargetY()){
					if(speed == 1 || speed == 2){
						return "SLOWER turn1 front";
					}
					else if(speed == 0){
						Coordinate Left1 = cMBR(this.coord, (this.rot+1)%6);
						Coordinate Left2 = cMBR(this.coord, (this.rot+2)%6);
						Coordinate Right1 = cMBR(this.coord, (this.rot+4)%6);
						Coordinate Right2 = cMBR(this.coord, (this.rot+5)%6);
						
						boolean left1Mine = false;
						boolean left2Mine = false;
						boolean right1Mine = false;
						boolean right2Mine = false;
						
						for(Mine m : mines){
							if(m.getX() == Left1.getX() && m.getY() == Left1.getY()){
								left1Mine = true;
							}
							if(m.getX() == Right1.getX() && m.getY() == Right1.getY()){
								right1Mine = true;
							}
							if(m.getX() == Left2.getX() && m.getY() == Left2.getY()){
								left1Mine = true;
							}
							if(m.getX() == Right2.getX() && m.getY() == Right2.getY()){
								right2Mine = true;
							}
						}
						if(left1Mine || right2Mine){
							return "STARBOARD turn1 front";
						}
						else if(left2Mine || right1Mine){
							return "PORT turn1 front";
						}
						else if( (!left1Mine) && (!left2Mine) && (!right1Mine) && (!right2Mine) ){
						    
						    return "PORT turn1 front";
						}
						else{
						    return "WAIT turn1 front";
						}
						
					}
				}
				//if back of ship will be hit next turn
				Coordinate check3 = cMBR(check1, ((this.rot+3)%6));
				if(check2.getX()==b.getTargetX() && check2.getY()==b.getTargetY()){
					if(speed==0 || speed==1){
						return "FASTE turn1 back";
					}
					else{ //speed = 2
						Coordinate Left1 = cMBR(this.coord, (this.rot+1)%6);
						Coordinate Left2 = cMBR(this.coord, (this.rot+2)%6);
						Coordinate Right1 = cMBR(this.coord, (this.rot+4)%6);
						Coordinate Right2 = cMBR(this.coord, (this.rot+5)%6);
						
						boolean left1Mine = false;
						boolean left2Mine = false;
						boolean right1Mine = false;
						boolean right2Mine = false;
						
						for(Mine m : mines){
							if(m.getX() == Left1.getX() && m.getY() == Left1.getY()){
								left1Mine = true;
							}
							if(m.getX() == Right1.getX() && m.getY() == Right1.getY()){
								right1Mine = true;
							}
							if(m.getX() == Left2.getX() && m.getY() == Left2.getY()){
								left1Mine = true;
							}
							if(m.getX() == Right2.getX() && m.getY() == Right2.getY()){
								right2Mine = true;
							}
						}
						if(left1Mine || right2Mine){
							return "STARBOARD turn1 back";
						}
						else if(left2Mine || right1Mine){
							return "PORT turn1 back";
						}
						else if( (!left1Mine) && (!left2Mine) && (!right1Mine) && (!right2Mine) ){
						    return "PORT turn1 back";
						}
						else{
						    return "WAIT turn1 back";
						}
					}
				}
			}
			else if(turns == 2){
			    //if center of ship will be hit in two turns
			    Coordinate check1 = positionAfterTurns(2);
			    if(check1.getX()==b.getTargetX() && check1.getY()==b.getTargetY()){
					if(this.speed == 1){
						return "FASTER turn2 center";
					}
					else if(this.speed == 2){
						return "SLOWER turn2 center";
					}
					else{
						return "FASTER turn2 center";
					}
				}
			    
			    
			    //if front of ship will be hit in two turns
			    Coordinate check2 = cMBR(check1, this.rot);
			    if(check2.getX()==b.getTargetX() && check2.getY()==b.getTargetY()){
			        if(this.speed == 2){
			            return "SLOWER turns2 front";   
			        }
			        else if(this.speed == 1){
			            //try to divert course, either PORT or STARBOARD   
			        }
			        else{
			            //try to divert course   
			        }
			    
			    //if back of ship will be hit in two turns
			    Coordinate check3 = cMBR(check1, ((this.rot+3)%6));
			        if(this.speed == 0 || this.speed ==1){
			            return "FASTER turns2 back";
			        }
			        else{
			            //try to divert course
			        }
			    }
			}
        }
        return "";
    }
    
    public boolean shipBlocking(ArrayList<Ship> ships, Coordinate c){
        return false;
    }
    
    public String move(ArrayList<Barrel> barrels, ArrayList<Mine> mines, ArrayList<Ball> balls){
        if(barrels.size()==0){
            return moveNoBarrels(mines, balls);
        }

        Barrel closest = null;
        boolean first = true;
        for(Barrel ba : barrels){
            if(first){
                closest = ba;
                first = false;
            }
            else{
                if(HexDistance.distance(this.getCoord(), ba.getCoord()) <
                   HexDistance.distance(this.getCoord(), closest.getCoord())
                ){
                    closest = ba;
                }
            }
        }
        
        return "MOVE " + closest.getX() + " " + closest.getY();
    }
    
    //handles movement when there are no barrels left
    public String moveNoBarrels(ArrayList<Mine> mines, ArrayList<Ball> balls){
        
        Coordinate frontOfShip = cMBR( this.getCoord(), this.getRot() );
		Coordinate nextFrontOfShip = cMBR( frontOfShip, this.getRot() );
        
        for (Mine m : mines) {
            if (m.getX() == nextFrontOfShip.getX() && m.getY() == nextFrontOfShip.getY()) {
                return "STARBOARD";
            }
        }
        
        if (endMove % 5 == 0) {
            Random rand = new Random();
            int MoveX = rand.nextInt((19 - 3) + 1) + 5;
            int MoveY = rand.nextInt((17 - 3) + 1) + 4;
            endMove++;
            return "MOVE " + MoveX + " " + MoveY;
        
        }
        endMove++;
        return "WAIT";
    }
        
    
    public String fire(ArrayList<Ship> ships) throws NullPointerException{
        Ship closest = null;
        boolean first = true;
        for(Ship s : ships){
            if(s.getFoe()==0){ //enemy ship
                if(first){
                    closest = s;
                    first = false;
                }
                else{
                    if(HexDistance.distance(this.getCoord(), s.getCoord()) <
                       HexDistance.distance(this.getCoord(), closest.getCoord())
                    ){
                        closest = s;
                    }
                }
            }
        }
        
        Coordinate frontOfShip = cMBR( this.getCoord(), this.getRot() );
        
        if(closest.getSpeed()==0 && HexDistance.distance(frontOfShip, closest.getCoord())<=10){
            return "FIRE "+closest.getX()+" "+closest.getY();
        }
        ArrayList<Coordinate> targets = closest.trajectory();
        
        for(int i = 0; i<targets.size(); i++){ 
            
            Coordinate target = targets.get(i);
            //check if HexDistance from front of ship to target is less than or equal to 10
            if(HexDistance.distance(frontOfShip, target)<=10){
            
                //how many turns for enemy to reach the point
                int enemyDistanceInTurns = (1 + HexDistance.distance(closest.getCoord(), target)/2);
                
                //how many turns for cannonball to go from front of ship to target
                double dist = HexDistance.distance(frontOfShip, target)/3.0;
                int cannonDistanceInTurns = 1 + (int)(Math.round(dist));
    
                
                if(enemyDistanceInTurns == cannonDistanceInTurns){
                    return "FIRE "+target.getX()+" " + target.getY();
                }  
            }
        }
        
        return "";
    } 
    
    public ArrayList<Coordinate> trajectory(){
        
        ArrayList<Coordinate> attackPoints = new ArrayList<Coordinate>();
        
        if(this.speed==0){
            attackPoints.add(this.getCoord());
            return attackPoints;
        }
        
        if(this.getRot()==0){
            for(int i = this.getX(); i<23; i++){
                attackPoints.add(new Coordinate(i, this.getY()));
            }
        }
        else if(this.getRot()==3){
            for(int i = this.getX(); i>=0; i--){
                attackPoints.add(new Coordinate(i, this.getY()));
            }
        }
        else if(this.getRot()==1){
            for(Coordinate i = this.getCoord(); i.getY()>=0 && i.getY()<21 && i.getX()<23 && i.getX()>=0; i = cMBR(i, 1)){
                attackPoints.add(i);
            }
        }
        else if(this.getRot()==2){
            for(Coordinate i = this.getCoord(); i.getY()>=0 && i.getY()<21 && i.getX()<23 && i.getX()>=0; i = cMBR(i, 2)){
                attackPoints.add(i);
            }
        }
        else if(this.getRot()==4){
            for(Coordinate i = this.getCoord(); i.getY()>=0 && i.getY()<21 && i.getX()<23 && i.getX()>=0; i = cMBR(i, 4)){
                attackPoints.add(i);
            }
        }
        else if(this.getRot()==5){
            for(Coordinate i = this.getCoord(); i.getY()>=0 && i.getY()<21 && i.getX()<23 && i.getX()>=0; i = cMBR(i, 5)){
                attackPoints.add(i);
            }
        }
        
        return attackPoints;
    }
    
    public static Coordinate cMBR(Coordinate c, int rot){
        if(rot==0){
            return new Coordinate(c.getX()+1, c.getY());
        }
        else if(rot==3){
            return new Coordinate(c.getX()-1, c.getY());
        }
        else if(rot==1){
            //odd-even coord rot=1
            if(c.getX()%2==1 && c.getY()%2==0){
                return new Coordinate(c.getX(), c.getY()-1);
            }
            //odd-odd coord rot=1
            if(c.getX()%2==1 && c.getY()%2==1){
                return new Coordinate(c.getX()+1, c.getY()-1);
            }
            //even-even coord rot=1
            if(c.getX()%2==0 && c.getY()%2==0){
                return new Coordinate(c.getX(), c.getY()-1);
            }
            //even-odd coord rot=1
            if(c.getX()%2==0 && c.getY()%2==1){
                return new Coordinate(c.getX()+1, c.getY()-1);
            }
        }
        else if(rot==2){
            //odd-even coord rot=2
            if(c.getX()%2==1 && c.getY()%2==0){
                return new Coordinate(c.getX()-1, c.getY()-1);
            }
            //odd-odd coord rot=2
            if(c.getX()%2==1 && c.getY()%2==1){
                return new Coordinate(c.getX(), c.getY()-1);
            }
            //even-even coord rot=2
            if(c.getX()%2==0 && c.getY()%2==0){
                return new Coordinate(c.getX()-1, c.getY()-1);
            }
            //even-odd coord rot=2
            if(c.getX()%2==0 && c.getY()%2==1){
                return new Coordinate(c.getX(), c.getY()-1);
            }
        }
        else if(rot==4){
            //odd-even coord rot=4
            if(c.getX()%2==1 && c.getY()%2==0){
                return new Coordinate(c.getX()-1, c.getY()+1);
            }
            //odd-odd coord rot=4
            if(c.getX()%2==1 && c.getY()%2==1){
                return new Coordinate(c.getX(), c.getY()+1);
            }
            //even-even coord rot=4
            if(c.getX()%2==0 && c.getY()%2==0){
                return new Coordinate(c.getX()-1, c.getY()+1);
            }
            //even-odd coord rot=4
            if(c.getX()%2==0 && c.getY()%2==1){
                return new Coordinate(c.getX(), c.getY()+1);
            }
        }
        else if(rot==5){
            //odd-even coord rot=5
            if(c.getX()%2==1 && c.getY()%2==0){
                return new Coordinate(c.getX(), c.getY()+1);
            }
            //odd-odd coord rot=5
            if(c.getX()%2==1 && c.getY()%2==1){
                return new Coordinate(c.getX()+1, c.getY()+1);
            }
            //even-even coord rot=5
            if(c.getX()%2==0 && c.getY()%2==0){
                return new Coordinate(c.getX(), c.getY()+1);
            }
            //even-odd coord rot=5
            if(c.getX()%2==0 && c.getY()%2==1){
                return new Coordinate(c.getX()+1, c.getY()+1);
            }
        }
        return null;
    }
	
	public Coordinate positionAfterTurns(int t){
		Coordinate current = this.coord;
		for(int i = 0; i<t; i++){
			for(int j = 0; j<this.speed; j++){
				current = cMBR(current, this.rot);
			}
		}
		return current;
	}
	
	public Coordinate neighbor(int rot){
		return cMBR(this.getCoord(), rot);
	}
    
    public Coordinate getCoord(){ return coord; }
    public int getX(){ return coord.getX(); }
    public int getY(){ return coord.getY(); }
    public int getRot(){ return rot; }
    public int getSpeed(){ return speed; }
    public int getRum(){ return rum; }
    public int getFoe(){ return foe; }
    public int getID(){ return id; }
    
}

class Barrel {
    private Coordinate coord;
    private int rum;
    private int id;

    public Barrel(int _x, int _y, int _r, int _i){
        coord = new Coordinate(_x,_y);
        rum = _r;
        id = _i;
    }
    
    public Coordinate getCoord(){ return coord; }
    public int getX(){ return coord.getX(); }
    public int getY(){ return coord.getY(); }
    public int getRum(){ return rum; }
    public int getID(){ return id; }

}

class Mine {
    private Coordinate coord;
    
    public Mine(int _x, int _y) {
        coord = new Coordinate(_x,_y);
    }
    
    public int getX(){ return coord.getX(); }
    public int getY(){ return coord.getY(); }
}
    
class Coordinate {
    private int x;
    private int y;
    
    public Coordinate(int x1, int y1){
        x = x1;
        y = y1;
    }
    
    public int getX(){ return x; }
    public int getY(){ return y; }   
}

class Ball {
    private Coordinate target;
    private int shipID;
    private int turnsToImpact;
    
    public Ball(int x, int y, int f, int t){
        target = new Coordinate(x,y);
        shipID = f;
        turnsToImpact = t;
    }
    
    public int getTargetX(){ return target.getX(); }
    public int getTargetY(){ return target.getY(); }
    public Coordinate getTarget(){ return target; }
    public int getShipID(){ return shipID; }
    public int getTurnsToImpact(){ return turnsToImpact; }
}


class Hex
{
    public Hex(int q, int r, int s)
    {
        this.q = q;
        this.r = r;
        this.s = s;
    }
    public final int q;
    public final int r;
    public final int s;

    static public Hex subtract(Hex a, Hex b)
    {
        return new Hex(a.q - b.q, a.r - b.r, a.s - b.s);
    }


    static public Hex scale(Hex a, int k)
    {
        return new Hex(a.q * k, a.r * k, a.s * k);
    }

    static public int length(Hex hex)
    {
        return (int)((Math.abs(hex.q) + Math.abs(hex.r) + Math.abs(hex.s)) / 2);
    }


    static public int distance(Hex a, Hex b)
    {
        return Hex.length(Hex.subtract(a, b));
    }

}

class OffsetCoord
{
    public OffsetCoord(int col, int row)
    {
        this.col = col;
        this.row = row;
    }
    public final int col;
    public final int row;
    static public int EVEN = 1;
    static public int ODD = -1;

    static public Hex roffsetToCube(int offset, OffsetCoord h)
    {
        int q = h.col - (int)((h.row + offset * (h.row & 1)) / 2);
        int r = h.row;
        int s = -q - r;
        return new Hex(q, r, s);
    }

}

class HexDistance
{
    static public int distance(Coordinate from, Coordinate to){
        return Hex.distance( 
            OffsetCoord.roffsetToCube(
                0,
                new OffsetCoord(
                    from.getX(), 
                    from.getY()
                )
            ), 
            OffsetCoord.roffsetToCube(
                0,
                new OffsetCoord(
                    to.getX(),
                    to.getY()
                )
            )
        );
    }
}