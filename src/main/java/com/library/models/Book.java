package com.library.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Book {
    private final IntegerProperty id;
    private final StringProperty title;
    private final StringProperty author;
    private final StringProperty isbn;
    private final IntegerProperty quantity;
    private final IntegerProperty availableQty;

   public Book (int id, String title, String author, String isbn, int quantity, int availableQty) {
       this.id = new SimpleIntegerProperty(id);
       this.title = new SimpleStringProperty(title);
       this.author = new SimpleStringProperty(author);
       this.isbn = new SimpleStringProperty(isbn);
       this.quantity = new SimpleIntegerProperty(quantity);
       this.availableQty = new SimpleIntegerProperty(availableQty);     }
    
    public int getId() { return id.get();    }
    public IntegerProperty idProperty() { return id;    }

    public String getTitle() { return title.get();    }
    public StringProperty titleProperty() { return title;    }

    public String getAuthor() { return author.get();    }
    public StringProperty authorProperty() { return author;    }

    public String getIsbn() { return isbn.get();    }
    public StringProperty isbnProperty() { return isbn;    }

    public int getQuantity() { return quantity.get();    }
    public IntegerProperty quantityProperty() { return quantity;    }

    public int getAvailableQty() { return availableQty.get();    }
    public IntegerProperty availableQtyProperty() { return availableQty;    }

    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }
}