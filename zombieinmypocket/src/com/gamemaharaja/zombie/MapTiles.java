package com.gamemaharaja.zombie;

// Class defines a room/outside tile in ZimP
// Loaded from a JSON file written in conjunction with textures
public class MapTiles {

	String  texturePath;	// texture path
	String 	name;			// name or the room or outside area	
	
	// TODO: require to have the texture cached here in OpenGL as a texture for quick load
	// Aalap require another variable here 
	
	// Sprite is a collection of all rooms into one big file for quicker loading
	// x, y, width and height are reqd. to locate the room image in the file 
	int		xPosInFile;		// position x in the file where this room starts
	int		yPosInFile;		// position y in the file where this room starts
	int		widthInFile;	// width of room in file
	int		heightInFile;	// ideally all width and height should be equal
	
	// Description of the Tile
	boolean startupTile;	// tile needs to be the first tile placed. (Foyer for standard game)
	
	// Addon tile that it can attach only to a specific tile (For eq: Patio in original ZimP)
	boolean isAddOnTile;	// Does this tile only link to an existing tile?
	String	baseTileName;	// Name of the tile that this tile attaches to?
	
	// Area transitions (from inside to outside)
	// Next tile will be from current area unless area transition capable
	String	area;						// Area of the map (inside/outside for the original ZimP)
	boolean isAreaTransitionCapable;	// Can tile cause area transition?	
	
	// Base Tile  - is this a base tile? (Like Dinning room in original ZimP)
	boolean isBaseTile;		// Does
	
	// Free Item Location
	boolean isFreeItem;		// Does tile provides a free item?
	String  freeItemText;	// Text related to getting the free item
	
	// Game Plot related actions
	boolean	isPlotItem;		// Does tile provide a plot item?
	String 	plotItemPreActionText;// Text that shows up when before plot item search?
	String	plotItemPostActionText;// Text that shows up when plot item is found
	String	plotItemName;	// Name of plot item that 
	String	[]plotReqdItems;	// Reqd item names that should be in possession to be able to proceed with plot
	String	[]plotMissingReqdText;	// If plot item is missing, mention that item is missing to user
	boolean	isPlotFinale;		// Is this the last tile after which game/stage is won?
	
	// Health Related Benefits/Deficits Rooms
	boolean healthItem;		// Does this tile provide a health change?
	String	healthText;		// Display message for text change
	int		healthChange;	// Change + or - in terms of health points
	
	boolean	[]exits;		// Exit Doors/Outdoor paths - always an array of 4
							// Array positions will change when tile is rotated
	String	[]exitConnectedTo;	// Name of the room connected to this tile 
	
	// Orientation
	int		rotation;		// 0, 90, 180, 270 Degrees of rotation while placement
							// When rotated texture and array is rotated
	
	// TODO: Aalap to make constructor of this class and set all fields reading from a file
	
	
	// Getter Setter functions
	public String getTexturePath() {
		return texturePath;
	}

	public void setTexturePath(String texturePath) {
		this.texturePath = texturePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getxPosInFile() {
		return xPosInFile;
	}

	public void setxPosInFile(int xPosInFile) {
		this.xPosInFile = xPosInFile;
	}

	public int getyPosInFile() {
		return yPosInFile;
	}

	public void setyPosInFile(int yPosInFile) {
		this.yPosInFile = yPosInFile;
	}

	public int getWidthInFile() {
		return widthInFile;
	}

	public void setWidthInFile(int widthInFile) {
		this.widthInFile = widthInFile;
	}

	public int getHeightInFile() {
		return heightInFile;
	}

	public void setHeightInFile(int heightInFile) {
		this.heightInFile = heightInFile;
	}

	public boolean isStartupTile() {
		return startupTile;
	}

	public void setStartupTile(boolean startupTile) {
		this.startupTile = startupTile;
	}

	public boolean isFreeItem() {
		return isFreeItem;
	}

	public void setFreeItem(boolean freeItem) {
		this.isFreeItem = freeItem;
	}

	public String getFreeItemText() {
		return freeItemText;
	}

	public void setFreeItemText(String freeItemText) {
		this.freeItemText = freeItemText;
	}

	public boolean isPlotItem() {
		return isPlotItem;
	}

	public void setPlotItem(boolean plotItem) {
		this.isPlotItem = plotItem;
	}

	public String getPlotItemPreActionText() {
		return plotItemPreActionText;
	}

	public void setPlotItemPreActionText(String plotItemPreActionText) {
		this.plotItemPreActionText = plotItemPreActionText;
	}

	public String getPlotItemPostActionText() {
		return plotItemPostActionText;
	}

	public void setPlotItemPostActionText(String plotItemPostActionText) {
		this.plotItemPostActionText = plotItemPostActionText;
	}

	public String getPlotItemName() {
		return plotItemName;
	}

	public void setPlotItemName(String plotItemName) {
		this.plotItemName = plotItemName;
	}

	public String[] getPlotReqdItems() {
		return plotReqdItems;
	}

	public void setPlotReqdItems(String[] plotReqdItems) {
		this.plotReqdItems = plotReqdItems;
	}

	public String[] getPlotMissingReqdText() {
		return plotMissingReqdText;
	}

	public void setPlotMissingReqdText(String[] plotMissingReqdText) {
		this.plotMissingReqdText = plotMissingReqdText;
	}

	public boolean isHealthItem() {
		return healthItem;
	}

	public void setHealthItem(boolean healthItem) {
		this.healthItem = healthItem;
	}

	public String getHealthText() {
		return healthText;
	}

	public void setHealthText(String healthText) {
		this.healthText = healthText;
	}

	public int getHealthChange() {
		return healthChange;
	}

	public void setHealthChange(int healthChange) {
		this.healthChange = healthChange;
	}

	public boolean[] getExits() {
		return exits;
	}

	public void setExits(boolean[] exits) {
		this.exits = exits;
	}

	public boolean isAddOnTile() {
		return isAddOnTile;
	}

	public void setAddOnTile(boolean isAddOnTile) {
		this.isAddOnTile = isAddOnTile;
	}

	public String getBaseTileName() {
		return baseTileName;
	}

	public void setBaseTileName(String baseTileName) {
		this.baseTileName = baseTileName;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public boolean isAreaTransitionCapable() {
		return isAreaTransitionCapable;
	}

	public void setAreaTransitionCapable(boolean isAreaTransitionCapable) {
		this.isAreaTransitionCapable = isAreaTransitionCapable;
	}

	public boolean isBaseTile() {
		return isBaseTile;
	}

	public void setBaseTile(boolean isBaseTile) {
		this.isBaseTile = isBaseTile;
	}

	public boolean isPlotFinale() {
		return isPlotFinale;
	}

	public void setPlotFinale(boolean isPlotFinale) {
		this.isPlotFinale = isPlotFinale;
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	
}
