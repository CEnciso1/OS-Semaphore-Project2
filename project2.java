import java.util.concurrent.Semaphore;
import java.util.Queue;
import java.util.LinkedList;
public class project2 {
    public static int count;
    public static final int NUMCUSTOMERS = 20;
    public static final int NUMAGENTS = 2;
    //  public static Queue<Thread> queue1 = new LinkedList<Thread>();
    public static Queue<Integer> queue1 = new LinkedList<Integer>();
    public static Queue<Integer> queue2 = new LinkedList<Integer>();
    public static Queue<Integer> aqueue = new LinkedList<Integer>();
    public static Semaphore mutex1 = new Semaphore(1, true);
    static Semaphore set_count = new Semaphore(0, true);
    static Semaphore mutex2 = new Semaphore(1, true);
    static Semaphore mutex3 = new Semaphore(1, true);
    static Semaphore desk_ready = new Semaphore(0, true);
    static Semaphore announcer_call = new Semaphore(0, true);
    static Semaphore line = new Semaphore(4, true);
    static Semaphore cust_ready = new Semaphore(0, true);
    static Semaphore agent_ready = new Semaphore(0, true);
    static Semaphore agent_done = new Semaphore(0, true);
    static Semaphore app_exams = new Semaphore(0, true);
    public static Semaphore [] agent_line = new Semaphore [50];
    static {
        for (int i = 0; i < 50; i++) {
            agent_line[i] = new Semaphore(0);
        }
    }
	public static Semaphore [] finished = new Semaphore [50];
    static {
        for(int i = 0; i < 50; i++) {
            finished[i] = new Semaphore(0);
        }   
    }
	

    public static void main(String args[]) {
        InformationDesk id = new InformationDesk();
        Thread id_thread = new Thread(id);
        id_thread.start();

        Announcer announcer = new Announcer();
        Thread announcer_thread = new Thread(announcer);
        announcer_thread.start();

        //Declare runnable and Thread objects
        Agent agents[] = new Agent[NUMAGENTS];
        Thread aThreads[] = new Thread[NUMAGENTS];

        //Create agent threads
        for (int i = 0; i < NUMAGENTS; i++) {
            agents[i] = new Agent(i);
            aThreads[i] = new Thread(agents[i], String.valueOf(i));
            aThreads[i].start();
        }


        //Declare runnable and Thread objects
        Customer customers[] = new Customer[NUMCUSTOMERS];
        Thread cThreads[] = new Thread[NUMCUSTOMERS];

        //Create customer threads
        for (int i = 0; i < NUMCUSTOMERS; i++) {
            customers[i] = new Customer(i);
            cThreads[i] = new Thread(customers[i], String.valueOf(i));
            cThreads[i].start();
        }

        for (int i = 0; i < NUMCUSTOMERS; ++i) {
            try {
                //System.out.println("Customer " + cThreads[i].getName() + " was joined");
                cThreads[i].join();
                System.out.println("Customer " + cThreads[i].getName() + " was joined");
            } catch (InterruptedException e) {
            } 
        }
    
        System.exit(0);
    }
}

class Customer implements Runnable {
    private int num;
				private int custnr;
				private int a_num;
    Customer(int num) {
        this.num = num;
        System.out.println("Customer " + num + " created, Enters DMV");
    }

    public void run() {
    
            try {
                project2.mutex1.acquire();
                project2.desk_ready.release();
                project2.set_count.acquire();
                custnr = project2.count;
                System.out.println("Customer " + num + " gets number " + custnr + ", enters waiting room");
                project2.mutex1.release();
                project2.queue1.add(custnr);
                project2.announcer_call.release();
                project2.mutex2.release();
                project2.line.acquire();
                project2.agent_line[custnr].acquire();
                System.out.println("Customer " + num + " moves to agent line");
                project2.mutex3.acquire();
                project2.queue2.add(num);
                project2.cust_ready.release();
                project2.mutex3.release();
                project2.agent_ready.acquire();
                a_num = project2.aqueue.poll();
                System.out.println("Customer " + num + " is being served by agent " + a_num);
                project2.app_exams.release();
                System.out.println("Customer " + num + " completes photo and eye exam for agent " + a_num);
                project2.finished[num].acquire();
                project2.agent_done.acquire();
                System.out.println("Customer " + num + " gets license and departs");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
    }
}

class InformationDesk implements Runnable {
    private int num;

    InformationDesk() {
        System.out.println("Information Desk Created");
    }

    public void run() {
        while (true) {
            try {
                project2.desk_ready.acquire();
                project2.count++;
                project2.set_count.release();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

class Announcer implements Runnable {
	private int a_cust;
	private int num;

	Announcer() {
		System.out.println("Announcer Created");
	}

	public void run() {
		while (true) {
			try {
				project2.announcer_call.acquire();
				project2.mutex2.acquire();
				a_cust = project2.queue1.poll();
				project2.mutex2.release();
				System.out.println("Announcer calls number " + a_cust);
				project2.agent_line[a_cust].release();
				project2.line.release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class Agent implements Runnable {
	private int num;
	private int ag_cust;
	Agent(int num) {
					this.num = num;
					System.out.println("Agent " + num + " created");
	}

	public void run() {
        while (true) {
            try {
                    project2.cust_ready.acquire();
                    project2.mutex3.acquire();
                    ag_cust = project2.queue2.poll();
                    project2.aqueue.add(num);
                    System.out.println("Agent " + num + " is serving customer " + ag_cust);
                    project2.agent_ready.release();
                    project2.mutex3.release();
                    System.out.println("Agent " + num + " asks customer " + ag_cust + " to take photo and eye exam");
                    project2.app_exams.acquire();
                    System.out.println("Agent " + num + " gives license to customer " + ag_cust);
                    project2.finished[ag_cust].release();
                    project2.agent_done.release();
            } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
            }
        }
	}
}