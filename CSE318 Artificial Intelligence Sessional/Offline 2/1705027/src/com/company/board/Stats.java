package com.company.board;

public class Stats {
    private int addtionalMove;
    private int capturedStones;
    private int binclosed_to_storage;
    private int stonesClosed_to_storage;

    public Stats() {
        binclosed_to_storage =0;
        capturedStones=0;
        addtionalMove=0;
        stonesClosed_to_storage=0;
    }

    public void initialize(){
        addtionalMove=0;
        capturedStones=0;
        binclosed_to_storage =0;
        stonesClosed_to_storage=0;
    }

    public int getStonesClosed_to_storage() {
        return stonesClosed_to_storage;
    }

    public void setStonesClosed_to_storage(int stonesClosed_to_storage) {
        this.stonesClosed_to_storage = stonesClosed_to_storage;
    }

    public int getBinclosed_to_storage() {
        return binclosed_to_storage;
    }

    public void setBinclosed_to_storage(int binclosed_to_storage) {
        this.binclosed_to_storage = binclosed_to_storage;
    }

    public int getCapturedStones() {
        return capturedStones;
    }

    public void setCapturedStones(int capturedStones) {
        this.capturedStones = capturedStones;
    }

    public void increaseAdditionalMove(){
        addtionalMove++;
    }

    public int getAddtionalMove(){
        return addtionalMove;
    }


}
