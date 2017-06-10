/**
 *
 * @author Ryan L.
 */

package com.ryanliang.inventorygui;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class InventoryModel extends AbstractTableModel implements Modellable {

	private Viewable view;
	private final ArrayList<Media> searchResult = new ArrayList<>();

	private static int IDCounter = 0;
	
	private final Connection connection;
	private PreparedStatement nonQueryStatement;
	private PreparedStatement queryStatement;
	private ResultSet resultSet;
	private ResultSetMetaData metaData;
	private int numberOfRows;
	
	private boolean connectedToDatabase = false;
	
	public InventoryModel(String url, String username, String password) throws SQLException {
		connection = DriverManager.getConnection(url, username, password);
		nonQueryStatement = null;
		queryStatement = null;
		//statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		connectedToDatabase = true;
	}
	
	@Override
	public void disconnectFromDatabase(){
		if (connectedToDatabase){
			try{
				if (resultSet != null)
					resultSet.close();
				if (nonQueryStatement != null)
					nonQueryStatement.close();
				if (queryStatement != null)
					queryStatement.close();
				if (connection != null)
					connection.close();
			}
			catch (SQLException sqlException){
				sqlException.printStackTrace();
			}
			finally{
				connectedToDatabase = false;
			}
		}
	}
	@Override
	public void addItem(Media media, String quantity) {
		if (media != null && quantity != null){
			String ID = media.getID();
			String sqlString = null;
	
			//Save data to database media tables
			setData(media, SQLCommand.INSERT);
			
			//Save data to database media inventory table
			if (!quantity.equals("") && Utility.isNumeric(quantity)){
				sqlString = "INSERT INTO inventory (MediaID, Quantity) VALUES(?, ?)";
				try {
					nonQueryStatement = connection.prepareStatement(sqlString);
					nonQueryStatement.setInt(1, Integer.valueOf(ID));
					nonQueryStatement.setInt(2, Integer.valueOf(quantity));
					nonQueryStatement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			//Save data to database media ID table
			sqlString = "UPDATE mediaID SET IDCounter = ? WHERE MediaID = 1";
			try {
				nonQueryStatement = connection.prepareStatement(sqlString);
				nonQueryStatement.setInt(1, ++IDCounter);
				nonQueryStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else 
			System.out.println("addItem(Media media, String quantityA) reference is null.");
	}
	
	private void setData(Media media, SQLCommand command) {		
		String none = "None";
		String sqlString = null;

		String ID = media.getID();
		String title = media.getTitle();
		String description = media.getDescription();
		String genre = media.getGenre();
		
		title = title.trim().equals("")?none:title;
		description = description.trim().equals("")?none:description;
		genre = genre.trim().equals("")?none:genre;
		
		if (media instanceof CD){
			String artist = ((CD) media).getArtist();

			artist = artist.trim().equals("")?none:artist;
			if (command == SQLCommand.UPDATE){
				sqlString = "UPDATE cd SET Title = ?, Description = ?, Genre = ?, Artist = ? WHERE CDID = ?";
				updateTable(sqlString, title, description, genre, artist, ID);
			}
			else if (command == SQLCommand.INSERT){
				sqlString = "INSERT INTO cd (CDID, Title, Description, Genre, Artist) VALUES(?, ?, ?, ?, ?)";
				insertToTable(sqlString, ID, title, description, genre, artist);
			}
		}
		else if (media instanceof DVD){
			String cast = ((DVD) media).getCast();
			
			cast = cast.trim().equals("")?none:cast;
			if (command == SQLCommand.UPDATE){
				sqlString = "UPDATE dvd SET Title = ?, Description = ?, Genre = ?, Cast = ? WHERE DVDID = ?";
				updateTable(sqlString, title, description, genre, cast, ID);
			}
			else if (command == SQLCommand.INSERT){
				sqlString = "INSERT INTO dvd (DVDID, Title, Description, Genre, Cast) VALUES(?, ?, ?, ?, ?)";
				insertToTable(sqlString, ID, title, description, genre, cast);
			}
		}
		else if (media instanceof Book){
			String author = ((Book) media).getAuthor();
			String ISBN = ((Book) media).getISBN();
			
			author = author.trim().equals("")?none:author;
			ISBN = ISBN.trim().equals("")?none:ISBN;
			if (command == SQLCommand.UPDATE){
				sqlString = "UPDATE book SET Title = ?, Description = ?, Genre = ?, Author = ?, ISBN = ? WHERE BookID = ?";
				updateTable(sqlString, title, description, genre, author, ISBN, ID);
			}
			else if (command == SQLCommand.INSERT){
				sqlString = "INSERT INTO book (BookID, Title, Description, Genre, Author, ISBN) VALUES(?, ?, ?, ?, ?, ?)";
				insertToTable(sqlString, ID, title, description, genre, author, ISBN);
			}
		}
	}
	
	private void insertToTable(String sqlString, String... parameters) {
		
		//***Do not modify this method until all callers of this method are not affected within this class.***
		
		int size = parameters.length;
		try {
			nonQueryStatement = connection.prepareStatement(sqlString);
			
			nonQueryStatement.setInt(1, Integer.valueOf(parameters[0]));
			for (int ii = 1; ii < size; ii++){
				nonQueryStatement.setString(ii+1, parameters[ii]);
			}
			nonQueryStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void updateTable(String sqlString, String... parameters) {
		
		//***Do not modify this method until all callers of this method are not affected within this class.***
		
		int size = parameters.length;
		try {
			nonQueryStatement = connection.prepareStatement(sqlString);
			
			for (int ii = 1; ii < size; ii++){
				nonQueryStatement.setString(ii, parameters[ii-1]);
			}
			nonQueryStatement.setInt(size, Integer.valueOf(parameters[size-1]));
			nonQueryStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void deleteFromTable(String sqlString, String... parameters) {
		
		//***Do not modify this method until all callers of this method are not affected within this class.***
		
		try {
			nonQueryStatement = connection.prepareStatement(sqlString);
			
			nonQueryStatement.setInt(1, Integer.valueOf(parameters[0]));
			nonQueryStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private boolean queryTable(MediaCategory media, String sqlString, String... parameters) {
		
		//***Do not modify this method until all callers of this method are not affected within this class.***
		
		int size = parameters.length;
		String ID = null;
		String title = "";
		String description = "";
		String genre = "";

		try {
			queryStatement = connection.prepareStatement(sqlString);
			
			if (size > 1){
				for (int ii = 1; ii <= size; ii++){
					queryStatement.setString(ii, "%" + parameters[ii-1] + "%");
				}
			}
			else if (size == 1 && !parameters[0].equals(""))	
				queryStatement.setInt(1, Integer.valueOf(parameters[0]));
			
			resultSet = queryStatement.executeQuery();
			metaData = resultSet.getMetaData();
			resultSet.last();
			numberOfRows = resultSet.getRow();
			resultSet.first();
			
			//NOTE: Below codes enable media item editing via a dialog.  Most of them can be removed if editing is done via JTable model only.
			if (numberOfRows > 0){
				do{
					if (resultSet.getObject(1) != null)
						ID = resultSet.getObject(1).toString();
					if (resultSet.getObject(2) != null)
						title = resultSet.getObject(2).toString();
					if (resultSet.getObject(3) != null)
						description = resultSet.getObject(3).toString();
					if (resultSet.getObject(4) != null)
						genre = resultSet.getObject(4).toString();
					if (resultSet.getObject(5) != null){
						if (media == MediaCategory.CD){
							String artist = resultSet.getObject(5).toString();
							searchResult.add(new CD(ID, title, description, genre, artist));
						}
						else if (media == MediaCategory.DVD){
							String cast = resultSet.getObject(5).toString();
							searchResult.add(new DVD(ID, title, description, genre, cast));
						}
						else if (media == MediaCategory.BOOK){
							String author = resultSet.getObject(5).toString();
							String ISBN = resultSet.getObject(6).toString();
							searchResult.add(new Book(ID, title, description, genre, author, ISBN));
						}
					}
					//Record match found.
					return true;
				}while (resultSet.next());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		//Record match not found.
		return false;
	}
	@Override
	public void editItem(Media media, String quantity) {
		if (media != null && quantity != null){
			String ID = media.getID();
			setData(media, SQLCommand.UPDATE);

			//Modify only if quantity is not empty and is a number (not consisting of alphabetic characters)
			String sqlString;
			if (!quantity.equals("") && Utility.isNumeric(quantity)){
				sqlString = "UPDATE inventory SET Quantity = ? WHERE MediaID = ?";
				try {
					nonQueryStatement = connection.prepareStatement(sqlString);
					nonQueryStatement.setInt(1, Integer.valueOf(quantity));
					nonQueryStatement.setInt(2, Integer.valueOf(ID));
					nonQueryStatement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		else 
			System.out.println("editItem(Media media, String quantity) reference is null.");
	}

	@Override
	public void saveData() {
	//not used at this time
	}
	
	@Override
	public void loadData() {		

		String sqlString;
		String counter;
		
		sqlString = "SELECT * FROM mediaID WHERE MediaID = ?";
		try {
			queryStatement = connection.prepareStatement(sqlString);
			queryStatement.setInt(1, 1);
			resultSet = queryStatement.executeQuery();
			resultSet.last();
			int numberOfRows = resultSet.getRow();
			resultSet.first();
			if (numberOfRows > 0){
				if (resultSet.getObject(2) != null){
					counter = resultSet.getObject(2).toString();
					if (counter != null)
						IDCounter = Integer.valueOf(counter);
				}
			}
			//Create MediaID "1" if the database is empty.  This happens only once during the life of this application.
			else{
				sqlString = "INSERT INTO mediaID (MediaID, IDCounter) VALUES(?, ?)";
				nonQueryStatement = connection.prepareStatement(sqlString);
				nonQueryStatement.setInt(1, 1);
				nonQueryStatement.setInt(2, 0);
				nonQueryStatement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void setView(Viewable view) {
		this.view = view;
	}
	
	@Override
	public void searchItem(String query) {
		if (query != null){
			query = query.trim();
			
			if (!query.equals("") && Utility.isNumeric(query)){
				searchItemHelper(query);
				view.update(UpdateType.SEARCH_RESULT);
			}
		}
		else 
			System.out.println("searchItem(String query) reference is null.");
	}
	
	@Override
	public void searchItem(String query, MediaCategory media) {
		if (query != null){
			query = query.trim();

			searchItemHelper(query, media);
			view.update(UpdateType.SEARCH_RESULT);
		}
		else 
			System.out.println("searchItemString query, MediaCategory media) reference is null.");
	}
	
	@Override
	public Media[] getSearchResult(){
		Media [] result = searchResult.toArray(new Media[searchResult.size()]);
		searchResult.clear();
		return result;
	}

	@Override
	public void deleteItem(String itemID) throws SQLException, IllegalStateException {
		if (itemID != null){
			itemID = itemID.trim();
			if (!itemID.equals("") && Utility.isNumeric(itemID)){
				String sqlString = "DELETE FROM cd WHERE CDID = ?";
				deleteFromTable(sqlString, itemID);
				
				sqlString = "DELETE FROM dvd WHERE DVDID = ?";
				deleteFromTable(sqlString, itemID);

				sqlString = "DELETE FROM book WHERE BookID = ?";
				deleteFromTable(sqlString, itemID);

				sqlString = "DELETE FROM inventory WHERE MediaID = ?";
				deleteFromTable(sqlString, itemID);
			}
		}
		else 
			System.out.println("deleteItem(String itemID) reference is null.");			
	}

	@Override
	public void generateID() {
		view.update(UpdateType.ID);	
	}
	
	@Override
	public String getID() {	
		return String.valueOf(IDCounter);
	}
	
	private void searchItemHelper(String query) {	
		String sqlString = null;

		//ID based search
		while (true){
			sqlString = "SELECT * FROM cd WHERE CDID = ?";
			if (queryTable(MediaCategory.CD, sqlString, query))
				break;
		
			sqlString = "SELECT * FROM dvd WHERE DVDID = ?";
			if (queryTable(MediaCategory.DVD, sqlString, query))
				break;

			sqlString = "SELECT * FROM book WHERE BookID = ?";
			if (queryTable(MediaCategory.BOOK, sqlString, query))
				break;

			break;
		}
	}
	
	private void searchItemHelper(String query, MediaCategory media) {
		String sqlString = null;

		//Word phrase based search
		if (!query.equals("")){
			if (media == MediaCategory.CD){
				sqlString = "SELECT * FROM cd WHERE Title LIKE ? OR Description LIKE ? OR Genre LIKE ? OR Artist LIKE ?";
				queryTable(MediaCategory.CD, sqlString, query, query, query, query);
			}
			else if (media == MediaCategory.DVD){
				sqlString = "SELECT * FROM dvd WHERE Title LIKE ? OR Description LIKE ? OR Genre LIKE ? OR Cast LIKE ?";
				queryTable(MediaCategory.DVD, sqlString, query, query, query, query);
			}
			else if (media == MediaCategory.BOOK){
				sqlString = "SELECT * FROM book WHERE Title LIKE ? OR Description LIKE ? OR Genre LIKE ? OR Author LIKE ? OR ISBN LIKE ?";
				queryTable(MediaCategory.BOOK, sqlString, query, query, query, query, query);
			}
		}
		//simply return all CDs, DVDs or Books
		else{
			if (media == MediaCategory.CD){
				sqlString = "SELECT * FROM cd";
				queryTable(MediaCategory.CD, sqlString, query);
			}
			else if (media == MediaCategory.DVD){
				sqlString = "SELECT * FROM dvd";
				queryTable(MediaCategory.DVD, sqlString, query);
			}
			else if (media == MediaCategory.BOOK){
				sqlString = "SELECT * FROM book";
				queryTable(MediaCategory.BOOK, sqlString, query);
			}
		}
	}

	@Override
	public void searchItemForEditing(String itemID) {		
		if (itemID != null){
			itemID = itemID.trim();
			if (!itemID.equals("") && Utility.isNumeric(itemID)){
				searchItemHelper(itemID);
				view.update(UpdateType.EDIT);
			}
		}
		else 
			System.out.println("searchItemForEditing(String itemID) reference is null.");
	}

	@Override
	public String getItemQuantity(String itemID) {

		String sqlString;
		String quantity = "0";
		if (itemID != null){
			itemID = itemID.trim();
			if (!itemID.equals("") && Utility.isNumeric(itemID)){
				sqlString = "SELECT * FROM inventory WHERE MediaID = ?";
				try {
					queryStatement = connection.prepareStatement(sqlString);
					queryStatement.setInt(1, Integer.valueOf(itemID));
					resultSet = queryStatement.executeQuery();
					resultSet.last();
					int numberOfRows = resultSet.getRow();
					resultSet.first();
					if (numberOfRows > 0){
						if (resultSet.getObject(2) != null){
							quantity = resultSet.getObject(2).toString();
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			return quantity;
		}
		else{ 
			System.out.println("getItemQuantity(String itemID) reference is null.");
			return null;
		}
	}
	
	@Override
	public String getColumnName(int column) 
	{ 
		try {
			return metaData.getColumnName(column+1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	} 

	@Override
	public int getRowCount() 
	{ 
		return numberOfRows;
	} 

	@Override
	public int getColumnCount() 
	{ 
		try {
			return metaData.getColumnCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	} 

	@Override
	public Object getValueAt(int row, int column) 
	{ 
		try {
			resultSet.absolute(row + 1);
			return resultSet.getObject(column +1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}
	@Override
	public Class getColumnClass(int column) 
	{ 
		try {
			String className = metaData.getColumnClassName(column + 1);
			return Class.forName(className);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Object.class;
	}
}

