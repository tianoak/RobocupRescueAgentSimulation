package csu.agent.fb.tools;

import java.util.List;
import java.util.Map;
import javolution.util.FastMap;

import org.uncommons.maths.number.NumberGenerator;
import org.uncommons.maths.random.GaussianGenerator;

import csu.agent.fb.FireBrigadeWorld;
import csu.model.object.CSUBuilding;

public class Simulator {
    
    public static float GAMMA = 0.2f;						// resq-fire.gamma
    public static float WATER_COEFFICIENT = 20.0f;			// resq-fire.water_thermal_capacity
    
    private static float BURN_RATE_AVERAGE =  0.15f;		// resq-fire.burn-rate-average
    private static float BURN_RATE_VARIANCE =  0.02f;		// resq-fire.burn-rate-variance
    private static int RANDOM_SEED = 23;					// resq-fire.randomseed
    
    private NumberGenerator<Double> burnRate = 
    		new GaussianGenerator(BURN_RATE_AVERAGE, BURN_RATE_VARIANCE, new java.util.Random(RANDOM_SEED));

    private FireBrigadeWorld world;
    
    /*private String str = null;*/
    
    public Simulator(FireBrigadeWorld world) {
        this.world = world;
    }

    /**
     * this method update building fuel and energy.
     * and get new fieriness and temperature like main fireSimulator.
     */
    public void update() {
    	/*str = "time = " + world.getTime() + ", agent = " + world.getControlledEntity();*/
        burn();
        cool(true);
        if (world.isNoRadio()) {
        	exchangeBuildingNoRadio();
        } else {
        	exchangeBuilding();
        }
        
        cool(false);
        
        for (CSUBuilding b : world.getCsuBuildings()) {
        	b.setVisible(false);
        }
        
        /*if (world.getControlledEntity().getID().getValue() == 928016686)
        	System.out.println(str);*/
    }

    private void burn() {
        double burnRate = this.burnRate.nextValue();
        for (CSUBuilding b : world.getCsuBuildings()) {
        	if (b.isVisible())
        		continue;
            if (b.getEstimatedTemperature() >= b.getIgnitionPoint() && b.isInflammable() && b.getFuel() > 0) {
            	/*if (b.getId().getValue() == 953) {
            		str = str + ", before burn, temperature = " + b.getEstimatedTemperature();
            	}*/
                
            	float consumed = b.getConsume(burnRate);
            	/*if (b.getId().getValue() == 953) {
                	str = str + ", energy = " + b.getEnergy();
                	str = str + ", consumed = " + consumed + ", fuel = " + b.getFuel();
                }*/
                
            	if (consumed > b.getFuel()) {
                    consumed = b.getFuel();
                }
                
                b.setEnergy(b.getEnergy() + consumed, "burn");
                b.setFuel(b.getFuel() - consumed);
                /*if (b.getId().getValue() == 953) {
                	str = str + ", after burn, temperature = " + b.getEstimatedTemperature();
                	str = str + ", energy = " + b.getEnergy() + ", fuel = " + b.getFuel();
                }*/
               
                b.setPrevBurned(consumed);
            } else {
                b.setPrevBurned(0f);
            }
        }
    }

    private void exchangeBuildingNoRadio() {
    	for (CSUBuilding b : world.getCsuBuildings()) {
            exchangeWithAir(b);
        }
    	for (CSUBuilding b : world.getCsuBuildings()) {
    		double radiationEnergy = b.getRadiationEnergy();
    		radiationEnergy /= 2.311738394;
    		b.setEnergy(b.getEnergy() - radiationEnergy, "exchangeBuildingNoRadio");
    	}
    }
    
    private void exchangeBuilding() {
        for (CSUBuilding b : world.getCsuBuildings()) {
            exchangeWithAir(b);
        }
        Map<CSUBuilding, Double> radiation = new FastMap<CSUBuilding, Double>();
        for (CSUBuilding b : world.getCsuBuildings()) {
            double radEn = b.getRadiationEnergy();
            
            /*if (b.getId().getValue() == 953) {
            	str = str + ", radiation energy = " + radEn;
            }*/
            
            radiation.put(b, radEn);
        }
        for (CSUBuilding b : world.getCsuBuildings()) {
            double radiationEnergy = radiation.get(b);
            if (world.isCommunicationLess() || world.isCommunicationMedium() || world.isCommunicationLow()) {
                radiationEnergy /= 2.311738394;
                /*if (b.getId().getValue() == 953) {
                	str = str + ", bad communication, radiation energy = " + radiationEnergy;
                }*/
            }
            List<CSUBuilding> bs = b.getConnectedBuildings();
            List<Float> vs = b.getConnectedValues();

            for (int c = 0; c < vs.size(); c++) {
            	CSUBuilding temp = bs.get(c);
                double oldEnergy = temp.getEnergy();
                double connectionValue = vs.get(c);
                double a = radiationEnergy * connectionValue;
                double sum = oldEnergy + a;
                bs.get(c).setEnergy(sum, "exchangeBuilding-recever");
            }
            
            b.setEnergy(b.getEnergy() - radiationEnergy, "exchangeBuilding-emitting");
        }
    }

    private void exchangeWithAir(CSUBuilding b) {

        double oldTemperature = b.getEstimatedTemperature();
        double oldEnergy = b.getEnergy();
        
        /*if (b.getId().getValue() == 953) {
        	str = str + ", before exchange with air, temp = " + b.getEstimatedTemperature();
        	str = str + ", energy = " + b.getEnergy();
        }*/

        if (oldTemperature > 100) {
            b.setEnergy(oldEnergy - (oldEnergy * 0.042), "exchangeWithAir");
        }
        
        /*if (b.getId().getValue() == 953) {
        	str = str + ", after exchange with air, temp = " + b.getEstimatedTemperature();
        	str = str + ", energy = " + b.getEnergy();
        }*/
    }

    private void cool(boolean first) {
        for (CSUBuilding building : world.getCsuBuildings()) {
        	if (building.isVisible()) {
        		building.setWaterQuantity(0);
        		continue;
        	}
            waterCooling(building);
        }
    }

    private void waterCooling(CSUBuilding b) {
        double lWATER_COEFFICIENT = (b.getEstimatedFieryness() > 0 && b.getEstimatedFieryness() < 4) ? 
        		WATER_COEFFICIENT: WATER_COEFFICIENT * GAMMA;
        
        /*if (b.getId().getValue() == 953) {
    		str = str + ", water_coefficient = " + WATER_COEFFICIENT;
    		str = str + ", gamma = " + GAMMA + ", l_water_coef = " + lWATER_COEFFICIENT;
    	}*/
        
        if (b.getWaterQuantity() > 0) {
            double dE = b.getEstimatedTemperature() * b.getCapacity();
            
            /*if (b.getId().getValue() == 953) {
        		str = str + ", before water cooling, temp = " + b.getEstimatedTemperature();
        		str = str + ", capacity = " + b.getCapacity() + ", dE = " + dE;
        		str = str + ", energy = " + b.getEnergy();
        	}*/
            
            if (dE <= 0) {
                return;
            }
            double effect = b.getWaterQuantity() * lWATER_COEFFICIENT;
            int consumed = b.getWaterQuantity();
            
            /*if (b.getId().getValue() == 953) {
            	str = str + ", water quantity = " + b.getWaterQuantity();
            	str = str + ", effect = " + effect + ", consumed = " + consumed;
            }*/
            
            if (effect > dE) {
                double pc = 1 - ((effect - dE) / effect);
                effect *= pc;
                consumed *= pc;
            }
            b.setWaterQuantity(b.getWaterQuantity() - consumed);
            b.setEnergy(b.getEnergy() - effect, "waterCooling");
            
            /*if (b.getId().getValue() == 953) {
            	str = str + ", after water cooling, energy = " + b.getEnergy();
            	str = str + ", temp = " + b.getEstimatedTemperature();
            	str = str + ", water quantuty = " + b.getWaterQuantity();
            	str = str + ", effect = " + effect + ", consumed = " + consumed;
            }*/
        }
    }
    
//    /**
//     * **********************************************************************************
//     * this method simulation temporary fire simulator for get data for next cycles.
//     * ya'ni in method vase pishbini ha va estefade dar select fire zone mibashad.
//     *
//     * @param zoneList: zone haei ke mikhaim rooshun shabih sazi anjam bedim.
//     */
//    public void tempSimulation(List<CsuZone> zoneList) {
//        simulationBurn(zoneList);
//        simulationExchangeBuilding(zoneList);
//    }
//
//    private void simulationBurn(List<CsuZone> zoneList) {
//        double bRate=burnRate.nextValue();
//        for (CsuZone zone : zoneList) {
//
//            for (CSUBuilding b : zone) {
//                if (!(b.getSelfBuilding() instanceof Refuge) && (b.getTempEstimatedTemperature() >= b.getIgnitionPoint() || b.isTempFlammable()) && b.getTempFuel() > 0) {
//                    float consumed = b.getTempConsume(bRate);
//                    if (consumed > b.getTempFuel()) {
//                        consumed = b.getTempFuel();
//                    }
//                    b.setTempEnergy(b.getTempEnergy() + consumed);
//                    b.setTempFuel(b.getTempFuel() - consumed);
//                    b.setTempPrevBurned(consumed);
//                } else {
//                    b.setTempPrevBurned(0f);
//                }
//            }
//        }
//    }
//
//    private void simulationExchangeBuilding(List<CsuZone> zoneList) {
//        for (CsuZone zone : zoneList) {
//            for (CSUBuilding b : zone) {
//                simulationExchangeWithAir(b);
//            }
//        }
//
//        for (CsuZone zone : zoneList) {
//            for (CSUBuilding b : zone) {
//                if ((b.getSelfBuilding() instanceof Refuge)) {
//                    continue;
//                }
//                double radEn = b.getTempRadiationEnergy();
//                List<CSUBuilding> bs = b.getConnectedBuildings();
//                List<Float> vs = b.getConnectedValues();
//
//                for (int c = 0; c < vs.size(); c++) {
//                    double oldEnergy = bs.get(c).getTempEnergy();
//                    double connectionValue = vs.get(c);
//                    double a = radEn * connectionValue;
//                    double sum = oldEnergy + a;
//                    bs.get(c).setTempEnergy(sum);
//                }
//                b.setTempEnergy(b.getTempEnergy() - radEn);
//            }
//        }
//    }
//
//    private void simulationExchangeWithAir(CSUBuilding b) {
//        // Give/take heat to/from air cells
//        double oldEnergy = b.getTempEnergy();
//        double energyDelta = (b.getSelfBuilding().getGroundArea() * 45 * (b.getSelfBuilding().getGroundArea() / 2500f));
//        double val = oldEnergy - energyDelta;
//        if (val < 0) {
//            val = 0;
//        }
//        b.setTempEnergy(val);
//    }
}
