package prodcons.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.beans.PropertyChangeSupport;
import java.lang.invoke.StringConcatFactory;

import javax.swing.event.SwingPropertyChangeSupport;

import prodcons.model.actor.StdConsumer;
import prodcons.model.actor.StdProducer;
import prodcons.util.Formatter;
import prodcons.util.event.SentenceEvent;
import prodcons.util.event.SentenceListener;
import util.Contract;

public class StdProdConsModel implements ProdConsModel {

    // ATTRIBUTS STATIQUES

    private static final int MAX_VALUE = 100;

    // ATTRIBUTS

    private final Actor[] actors;
    private final Box box;
    private final int prodNumber;
    private final int consNumber;
    private final PropertyChangeSupport support;

    private volatile boolean running;
    private volatile boolean isFrozen;

    // CONSTRUCTEURS

    public StdProdConsModel(int prod, int cons, int iter) {
        Contract.checkCondition(prod > 0 && cons > 0 && iter > 0);

        box = new UnsafeBox();
        prodNumber = prod;
        consNumber = cons;
        
        actors = new Actor[prodNumber + consNumber];
        SentenceListener sl= new SentenceListener() {
			@Override
			public void sentenceSaid(SentenceEvent e) {
				String s= e.getSentence();
				support.firePropertyChange(PROP_SENTENCE, null, s);
			}
		};
		FrozenDetector fd =new FrozenDetector();
        
        for (int i = 0; i < prodNumber; i++) {
            actors[i] = new StdProducer(iter, MAX_VALUE, box);
            actors[i].addSentenceListener(sl);
            actors[i].addPropertyChangeListener(Actor.PROP_ACTIVE, fd);
        }
        for (int i = prodNumber; i < prodNumber + consNumber; i++) {
            actors[i] = new StdConsumer(iter, box);
            actors[i].addSentenceListener(sl);
            actors[i].addPropertyChangeListener(Actor.PROP_ACTIVE, fd);
        }
        support = new SwingPropertyChangeSupport(this, true);
    }
    // REQUETES

    @Override
    public Box box() {
        return box;
    }

    @Override
    public Actor consumer(int i) {
        Contract.checkCondition(0 <= i && i < consNumber);

        return actors[prodNumber + i];
    }

    @Override
    public int consumersNb() {
        return consNumber;
    }

    @Override
    public boolean isFrozen() {
        /*****************/
    	return isFrozen;
        /*****************/
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public Actor producer(int i) {
        Contract.checkCondition(0 <= i && i < prodNumber);

        return actors[i];
    }

    @Override
    public int producersNb() {
        return prodNumber;
    }
    
    // COMMANDES

    @Override
    public void addPropertyChangeListener(String pName,
                PropertyChangeListener lnr) {
        Contract.checkCondition(pName != null && lnr != null);

        support.addPropertyChangeListener(pName, lnr);
    }   

    @Override
    public void start() {
        /*****************/  	
    	box.clear();
    	Formatter.resetTime();
    	setFrozen(false);
    	setRunning(true);
    	for (int i = 0; i < actors.length; i++) {
			actors[i].start();
		}
        /*****************/
    }

    @Override
    public void stop() {
        /*****************/
    	Thread t =new Thread(new EraserTask());
    	t.start();
    	setRunning(false);
        /*****************/
    }
    
	private void setFrozen(boolean b) {
    	boolean oldFrozen=isFrozen();
    	isFrozen=b;
    	support.firePropertyChange(PROP_FROZEN, oldFrozen, b);
    }
    

    // OUTILS
	

	private void setRunning(boolean b) {
        boolean oldRunning = isRunning();
        running = b;
        support.firePropertyChange(PROP_RUNNING, oldRunning, b);
    }
    
    
    private class FrozenDetector  implements PropertyChangeListener{
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			setFrozen(computeFrozen());
			if (isFrozen) {
				Thread thread = new Thread(new EraserTask());
				thread.start();
				setRunning(false);
			}
		}
		
		private boolean allProdDead() {
			for (int i = 0; i < prodNumber ; i++) {
				if (actors[i].isAlive()) {
					return false; 
				}
			}
			return true;
		}
		private boolean allConsDead() {
			for (int i = prodNumber; i <prodNumber+consNumber; i++) {
				if (actors[i].isAlive()) {
					return false; 
				}
			}
			return true;
		}
		private boolean prodAliveFrozen() {
			for (int i = 0; i <prodNumber; i++) {
				if (actors[i].isAlive() && actors[i].isActive()) { 
					return false; 
				}
			}
			return true;
		}
		private boolean consAliveFrozen() {
			for (int i = prodNumber; i <prodNumber+consNumber; i++) {
				if (actors[i].isAlive() && actors[i].isActive()) {
					return false; 
				}
			}
			return true;
		}	
				
		private boolean computeFrozen() {
			return allProdDead() && consAliveFrozen() || allConsDead() && prodAliveFrozen();
		}
    	
    }
    private class EraserTask  implements Runnable{
    	@Override
    	public void run() {
    		for (Actor actor : actors) {
    			if (actor.isAlive()) {
    				actor.interruptAndWaitForTermination();
    			}
    		}
    	}
    }
    
    
}
