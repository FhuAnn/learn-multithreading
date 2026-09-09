public class RaceCondition {
    public static void main(String[] args) throws InterruptedException {
         InventoryCounter inventoryCounter = new InventoryCounter() ;
         IncrementingThread incrementingThred = new IncrementingThread(inventoryCounter);
            DecrementingThread decrementingThread = new DecrementingThread(inventoryCounter);
            incrementingThred.start();
            decrementingThread.start();
            incrementingThred.join();
            decrementingThread.join();
        System.out.println("We currently have "+ inventoryCounter.getItems() +" items");
    }

    public static class DecrementingThread extends Thread {
        private InventoryCounter inventoryCounter;
        public DecrementingThread(InventoryCounter inventoryCounter) {
            this.inventoryCounter = inventoryCounter;
        }
        @Override
        public void run() {
            for(int i =0 ;i <1000; i ++)
            {
                inventoryCounter.decrement();
            }
        }
    }
    public static class IncrementingThread extends Thread {
        private InventoryCounter inventoryCounter;
        public IncrementingThread(InventoryCounter inventoryCounter) {
            this.inventoryCounter = inventoryCounter;
        }
        @Override
        public void run() {
            for(int i =0 ;i <1000; i ++)
            {
                inventoryCounter.increment();
            }
        }
    }
    private static class InventoryCounter {
        private int items =0;
        public void increment() {
            items++;
        }
        public void decrement() {
            items--;
        }
        public int getItems(){
            return items;
        }
    }
}
