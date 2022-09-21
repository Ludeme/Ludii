package ai.chunk;

import java.util.Vector;

import database.GameDatabase;

public class ChunkTable {
	
	public class ChunkTableEntry
	{
		public String player;
		public Vector<Chunk> chunks;
	}
	
	Vector<ChunkTableEntry> entries = new Vector();
	
	public int size()
	{
		return entries.size();
	}
	
	public ChunkTableEntry get(int k)
	{
		return entries.get(k);
	}
	
	public ChunkTableEntry lookup(String player)
	{
		for (int k=0; k<size(); k++)
		{
			if (player.equals(get(k).player)) return get(k);
		}
		return null;
	}
	
	public void add(String s, Vector<Chunk> c)
	{
		if (lookup(s)!=null) return;
		
		ChunkTableEntry t = new ChunkTableEntry();
		t.player = s;
		t.chunks = c;
		entries.add(t);
	}
	
	public void loadFromDatabase(String s, GameDatabase gd)
	{
		if (lookup(s)!=null) return;
		
		Vector<Chunk> c = ChunkMaker.makeChunks(s, gd);
		add(s,c);
	}

}
