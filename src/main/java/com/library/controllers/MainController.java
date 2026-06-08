package com.library.controllers;

import com.library.database.DBConnection;
import com.library.models.Book;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.sql.*;

public class MainController {

    public static class Member {
        private final IntegerProperty memberId = new SimpleIntegerProperty();
        private final StringProperty  surname  = new SimpleStringProperty();
        private final StringProperty  name     = new SimpleStringProperty();
        private final StringProperty  phone    = new SimpleStringProperty();
        private final StringProperty  email    = new SimpleStringProperty();
        private final StringProperty  regDate  = new SimpleStringProperty();

        public Member(int memberId, String surname, String name,
                      String phone, String email, String regDate) {
            this.memberId.set(memberId);
            this.surname .set(surname);
            this.name    .set(name);
            this.phone   .set(phone);
            this.email   .set(email);
            this.regDate .set(regDate);
        }

        public IntegerProperty memberIdProperty() { return memberId; }
        public StringProperty  surnameProperty()  { return surname;  }
        public StringProperty  nameProperty()     { return name;     }
        public StringProperty  phoneProperty()    { return phone;    }
        public StringProperty  emailProperty()    { return email;    }
        public StringProperty  regDateProperty()  { return regDate;  }

        public int    getMemberId() { return memberId.get(); }
        public String getSurname()  { return surname .get(); }
        public String getName()     { return name    .get(); }
        public String getPhone()    { return phone   .get(); }
        public String getEmail()    { return email   .get(); }
        public String getRegDate()  { return regDate .get(); }
    }

    public static class BorrowRecord {
        private final IntegerProperty recordId   = new SimpleIntegerProperty();
        private final StringProperty  borrowDate = new SimpleStringProperty();
        private final StringProperty  dueDate    = new SimpleStringProperty();
        private final StringProperty  returnDate = new SimpleStringProperty();
        private final StringProperty  status     = new SimpleStringProperty();
        private final IntegerProperty bookId     = new SimpleIntegerProperty();
        private final IntegerProperty memberId   = new SimpleIntegerProperty();
        private final StringProperty  bookName   = new SimpleStringProperty();   // НЭМСЭН: номын нэр
        private final StringProperty  memberName = new SimpleStringProperty();   // НЭМСЭН: уншигчийн нэр

        public BorrowRecord(int recordId, String borrowDate, String dueDate,
                            String returnDate, String status, int bookId, int memberId,
                            String bookName, String memberName) {
            this.recordId  .set(recordId);
            this.borrowDate.set(borrowDate);
            this.dueDate   .set(dueDate);
            this.returnDate.set(returnDate != null ? returnDate : "");
            this.status    .set(status);
            this.bookId    .set(bookId);
            this.memberId  .set(memberId);
            this.bookName  .set(bookName   != null ? bookName   : "");
            this.memberName.set(memberName != null ? memberName : "");
        }

        public IntegerProperty recordIdProperty()   { return recordId;   }
        public StringProperty  borrowDateProperty() { return borrowDate; }
        public StringProperty  dueDateProperty()    { return dueDate;    }
        public StringProperty  returnDateProperty() { return returnDate; }
        public StringProperty  statusProperty()     { return status;     }
        public IntegerProperty bookIdProperty()     { return bookId;     }
        public IntegerProperty memberIdProperty()   { return memberId;   }
        public StringProperty  bookNameProperty()   { return bookName;   }
        public StringProperty  memberNameProperty() { return memberName; }

        public int    getRecordId()   { return recordId  .get(); }
        public String getBorrowDate() { return borrowDate.get(); }
        public String getDueDate()    { return dueDate   .get(); }
        public String getReturnDate() { return returnDate.get(); }
        public String getStatus()     { return status    .get(); }
        public int    getBookId()     { return bookId    .get(); }
        public int    getMemberId()   { return memberId  .get(); }
        public String getBookName()   { return bookName  .get(); }
        public String getMemberName() { return memberName.get(); }
    }

    @FXML private TextField txtBookName, txtAuthor, txtIsb, txtQauntity, txtSearch;

    @FXML private TableView<Book>            tableBook;
    @FXML private TableColumn<Book, Integer> colBookNumber, colQuantity, colAvailableQty;
    @FXML private TableColumn<Book, String>  colBookName, colAuthor, colIsbn;

    private final ObservableList<Book> bookList = FXCollections.observableArrayList();

    @FXML private TextField txtSurname, txtName, txtPhone, txtEmail, txtMemberSearch;

    @FXML private TableView<Member>            tableMember;
    @FXML private TableColumn<Member, Integer> colMemberId;
    @FXML private TableColumn<Member, String>  colSurName, colName, colPhone, colEmail, colDate;

    private final ObservableList<Member> memberList = FXCollections.observableArrayList();

    @FXML private RadioButton radioAll, radioOverdue;
    @FXML private ToggleGroup filterGroup;

    @FXML private TableView<BorrowRecord>            tableRent;
    @FXML private TableColumn<BorrowRecord, Integer> colRecord_id;
    // ЗАСВАР: colBook_id, colMember_id-г String болгож шилжүүлсэн (ID-ийн оронд нэр харуулна)
    @FXML private TableColumn<BorrowRecord, String>  colBorrow_date, colDue_date,
                                                      colReturn_date, colStatus,
                                                      colBook_id, colMember_id;

    private final ObservableList<BorrowRecord> rentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupBookTable();
        setupMemberTable();
        setupRentTable();
        loadBooks();
        loadMembers();
        loadRentRecords("all");
    }

    private void setupBookTable() {
        colBookNumber  .setCellValueFactory(d -> d.getValue().idProperty().asObject());
        colBookName    .setCellValueFactory(d -> d.getValue().titleProperty());
        colAuthor      .setCellValueFactory(d -> d.getValue().authorProperty());
        colIsbn        .setCellValueFactory(d -> d.getValue().isbnProperty());
        colQuantity    .setCellValueFactory(d -> d.getValue().quantityProperty().asObject());
        colAvailableQty.setCellValueFactory(d -> d.getValue().availableQtyProperty().asObject());

        FilteredList<Book> filtered = new FilteredList<>(bookList, b -> true);
        tableBook.setItems(filtered);

        txtSearch.textProperty().addListener((obs, o, nv) ->
            filtered.setPredicate(book -> {
                if (nv == null || nv.isBlank()) return true;
                String f = nv.toLowerCase();
                return book.getTitle() .toLowerCase().contains(f)
                    || book.getAuthor().toLowerCase().contains(f)
                    || book.getIsbn()  .toLowerCase().contains(f);
            })
        );
    }

    private void setupMemberTable() {
        colMemberId.setCellValueFactory(d -> d.getValue().memberIdProperty().asObject());
        colSurName .setCellValueFactory(d -> d.getValue().surnameProperty());
        colName    .setCellValueFactory(d -> d.getValue().nameProperty());
        colPhone   .setCellValueFactory(d -> d.getValue().phoneProperty());
        colEmail   .setCellValueFactory(d -> d.getValue().emailProperty());
        colDate    .setCellValueFactory(d -> d.getValue().regDateProperty());

        FilteredList<Member> filtered = new FilteredList<>(memberList, m -> true);
        tableMember.setItems(filtered);

        txtMemberSearch.textProperty().addListener((obs, o, nv) ->
            filtered.setPredicate(m -> {
                if (nv == null || nv.isBlank()) return true;
                String f = nv.toLowerCase();
                return m.getSurname().toLowerCase().contains(f)
                    || m.getName()   .toLowerCase().contains(f)
                    || m.getPhone()  .toLowerCase().contains(f);
            })
        );
    }

    private void setupRentTable() {
        colRecord_id  .setCellValueFactory(d -> d.getValue().recordIdProperty().asObject());
        colBorrow_date.setCellValueFactory(d -> d.getValue().borrowDateProperty());
        colDue_date   .setCellValueFactory(d -> d.getValue().dueDateProperty());
        colReturn_date.setCellValueFactory(d -> d.getValue().returnDateProperty());
        colStatus     .setCellValueFactory(d -> d.getValue().statusProperty());
        // ЗАСВАР: ID-ийн оронд нэр харуулна
        colBook_id    .setCellValueFactory(d -> d.getValue().bookNameProperty());
        colMember_id  .setCellValueFactory(d -> d.getValue().memberNameProperty());
        tableRent.setItems(rentList);

        // radio-уудыг нэг ToggleGroup-д баттай холбоно (FXML-д байхгүй байсан ч энд үүсгэнэ).
        if (filterGroup == null) {
            filterGroup = new ToggleGroup();
        }
        radioAll.setToggleGroup(filterGroup);
        radioOverdue.setToggleGroup(filterGroup);
        radioAll.setSelected(true);   // анхдагчаар "Бүгд" (listener-ээс ӨМНӨ тул давхар ачаалахгүй)

        filterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == radioOverdue) {
                loadRentRecords("overdue");
            } else {
                loadRentRecords("all");   // radioAll эсвэл null үед
            }
        });
    }

    private void loadBooks() {
        bookList.clear();
        String sql = "SELECT book_id, title, author, isbn, quantity, available_qty FROM book";
        try (Connection c = DBConnection.getConnection();
             Statement s  = c.createStatement();
             ResultSet r  = s.executeQuery(sql)) {
            while (r.next()) {
                bookList.add(new Book(
                    r.getInt("book_id"),    r.getString("title"),
                    r.getString("author"),  r.getString("isbn"),
                    r.getInt("quantity"),   r.getInt("available_qty")
                ));
            }
        } catch (Exception e) {
            showAlert("Алдаа", "Ном ачаалахад алдаа:\n" + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void btnAddBook(ActionEvent event) {
        String title = txtBookName.getText().trim();
        String auth  = txtAuthor  .getText().trim();
        String isbn  = txtIsb     .getText().trim();
        String qtyS  = txtQauntity.getText().trim();

        if (title.isEmpty() || auth.isEmpty() || isbn.isEmpty() || qtyS.isEmpty()) {
            showAlert("Анхааруулга", "Бүрэн мэдээлэл оруулна уу.", AlertType.WARNING);
            return;
        }
        try {
            int qty = Integer.parseInt(qtyS);
            String sql = "INSERT INTO book (title, author, isbn, quantity, available_qty) VALUES (?,?,?,?,?)";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, title); ps.setString(2, auth);
                ps.setString(3, isbn);  ps.setInt(4, qty); ps.setInt(5, qty);
                ps.executeUpdate();
            }
            showAlert("Амжилттай", "Ном нэмэгдлээ.", AlertType.INFORMATION);
            clearBookFields();
            loadBooks();
        } catch (NumberFormatException e) {
            showAlert("Алдаа", "Тоо ширхэгт зөв тоо оруулна уу.", AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Алдаа", "Алдаа гарлаа:\n" + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void btnEditBook(ActionEvent event) {
        Book sel = tableBook.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Анхааруулга", "Засах номоо сонгоно уу.", AlertType.WARNING);
            return;
        }

        Dialog<Book> dlg = new Dialog<>();
        dlg.setTitle("Ном засах");
        dlg.setHeaderText("Мэдээллээ засна уу:");
        ButtonType save = new ButtonType("Хадгалах", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField fT = new TextField(sel.getTitle()),
                  fA = new TextField(sel.getAuthor()),
                  fI = new TextField(sel.getIsbn()),
                  fQ = new TextField(String.valueOf(sel.getQuantity()));
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10);
        g.addRow(0, new Label("Нэр:"),        fT);
        g.addRow(1, new Label("Зохиолч:"),    fA);
        g.addRow(2, new Label("ISBN:"),        fI);
        g.addRow(3, new Label("Тоо ширхэг:"), fQ);
        dlg.getDialogPane().setContent(g);

        dlg.setResultConverter(b -> {
            if (b == save) {
                try {
                    int nq = Integer.parseInt(fQ.getText().trim());
                    int av = Math.max(0, Math.min(nq, sel.getAvailableQty() + nq - sel.getQuantity()));
                    return new Book(sel.getId(), fT.getText().trim(),
                                    fA.getText().trim(), fI.getText().trim(), nq, av);
                } catch (NumberFormatException ex) {
                    showAlert("Алдаа", "Тоо оруулна уу.", AlertType.ERROR);
                }
            }
            return null;
        });

        dlg.showAndWait().ifPresent(u -> {
            String sql = "UPDATE book SET title=?, author=?, isbn=?, quantity=?, available_qty=? WHERE book_id=?";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, u.getTitle());    ps.setString(2, u.getAuthor());
                ps.setString(3, u.getIsbn());     ps.setInt(4, u.getQuantity());
                ps.setInt(5, u.getAvailableQty()); ps.setInt(6, u.getId());
                ps.executeUpdate();
                showAlert("Амжилттай", "Ном засагдлаа.", AlertType.INFORMATION);
                loadBooks();
            } catch (Exception e) {
                showAlert("Алдаа", e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            }
        });
    }

    @FXML
    void btnDeleteBook(ActionEvent event) {
        Book sel = tableBook.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Анхааруулга", "Устгах номоо сонгоно уу.", AlertType.WARNING);
            return;
        }

        // ЗАСВАР: идэвхтэй түрээстэй (буцаагдаагүй) номыг устгахыг хориглоно.
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM borrow_record WHERE bookid=? AND return_date IS NULL")) {
            ps.setInt(1, sel.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert("Боломжгүй",
                        "Энэ ном одоогоор түрээслэгдсэн байна. Бүх хувийг буцааж авсны дараа устгана уу.",
                        AlertType.WARNING);
                    return;
                }
            }
        } catch (Exception e) {
            showAlert("Алдаа", "Шалгахад алдаа:\n" + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
            return;   // шалгалт амжилтгүй бол устгахгүй (аюулгүй тал руугаа)
        }

        Alert cf = new Alert(AlertType.CONFIRMATION);
        cf.setHeaderText(null);
        cf.setContentText("\"" + sel.getTitle() + "\" устгах уу?");
        cf.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM book WHERE book_id=?")) {
                ps.setInt(1, sel.getId());
                ps.executeUpdate();
                showAlert("Амжилттай", "Ном устгагдлаа.", AlertType.INFORMATION);
                loadBooks();
            } catch (Exception e) {
                showAlert("Алдаа", e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            }
        });
    }

    private void loadMembers() {
        memberList.clear();
        String sql = "SELECT member_id, surname, name, phone, email, reg_date FROM member";
        try (Connection c = DBConnection.getConnection();
             Statement s  = c.createStatement();
             ResultSet r  = s.executeQuery(sql)) {
            while (r.next()) {
                memberList.add(new Member(
                    r.getInt("member_id"),  r.getString("surname"),
                    r.getString("name"),    r.getString("phone"),
                    r.getString("email"),   r.getString("reg_date")
                ));
            }
        } catch (Exception e) {
            showAlert("Алдаа", "Уншигч ачаалахад алдаа:\n" + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void btnAddMember(ActionEvent event) {
        String sur   = txtSurname.getText().trim();
        String name  = txtName   .getText().trim();
        String phone = txtPhone  .getText().trim();
        String email = txtEmail  .getText().trim();

        if (sur.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            showAlert("Анхааруулга", "Овог, нэр, утас заавал оруулна уу.", AlertType.WARNING);
            return;
        }
        String sql = "INSERT INTO member (surname, name, phone, email, reg_date) VALUES (?,?,?,?,CURDATE())";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sur);   ps.setString(2, name);
            ps.setString(3, phone); ps.setString(4, email);
            ps.executeUpdate();
            showAlert("Амжилттай", "Уншигч нэмэгдлээ.", AlertType.INFORMATION);
            clearMemberFields();
            loadMembers();
        } catch (Exception e) {
            showAlert("Алдаа", "Уншигч нэмэхэд алдаа:\n" + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void btnEditMember(ActionEvent event) {
        Member sel = tableMember.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Анхааруулга", "Засах уншигчаа сонгоно уу.", AlertType.WARNING);
            return;
        }

        Dialog<Member> dlg = new Dialog<>();
        dlg.setTitle("Уншигч засах");
        dlg.setHeaderText("Мэдээллээ засна уу:");
        ButtonType save = new ButtonType("Хадгалах", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        TextField fS = new TextField(sel.getSurname()),
                  fN = new TextField(sel.getName()),
                  fP = new TextField(sel.getPhone()),
                  fE = new TextField(sel.getEmail());
        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10);
        g.addRow(0, new Label("Овог:"),  fS);
        g.addRow(1, new Label("Нэр:"),   fN);
        g.addRow(2, new Label("Утас:"),  fP);
        g.addRow(3, new Label("Email:"), fE);
        dlg.getDialogPane().setContent(g);

        dlg.setResultConverter(b -> b == save
            ? new Member(sel.getMemberId(), fS.getText().trim(), fN.getText().trim(),
                         fP.getText().trim(), fE.getText().trim(), sel.getRegDate())
            : null);

        dlg.showAndWait().ifPresent(u -> {
            String sql = "UPDATE member SET surname=?, name=?, phone=?, email=? WHERE member_id=?";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, u.getSurname()); ps.setString(2, u.getName());
                ps.setString(3, u.getPhone());   ps.setString(4, u.getEmail());
                ps.setInt(5, u.getMemberId());
                ps.executeUpdate();
                showAlert("Амжилттай", "Уншигч засагдлаа.", AlertType.INFORMATION);
                loadMembers();
            } catch (Exception e) {
                showAlert("Алдаа", e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            }
        });
    }

    @FXML
    void btnDeleteMember(ActionEvent event) {
        Member sel = tableMember.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Анхааруулга", "Устгах уншигчаа сонгоно уу.", AlertType.WARNING);
            return;
        }
        Alert cf = new Alert(AlertType.CONFIRMATION);
        cf.setHeaderText(null);
        cf.setContentText(sel.getSurname() + " " + sel.getName() + "-г устгах уу?");
        cf.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM member WHERE member_id=?")) {
                ps.setInt(1, sel.getMemberId());
                ps.executeUpdate();
                showAlert("Амжилттай", "Уншигч устгагдлаа.", AlertType.INFORMATION);
                loadMembers();
            } catch (Exception e) {
                showAlert("Алдаа", e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            }
        });
    }

    private void loadRentRecords(String filter) {
        rentList.clear();
        // ЗАСВАР: book, member-тэй LEFT JOIN хийж нэрийг авна.
        // LEFT JOIN — ном/уншигч устсан байсан ч түрээсийн бичлэг алга болохгүй (нэр хоосон харагдана).
        // "Хугацаа хэтэрсэн" = буцаагаагүй (return_date IS NULL) + буцаах хугацаа өнгөрсөн.
        String base =
            "SELECT br.record_id, br.borrow_date, br.due_date, br.return_date, br.status, " +
            "       br.bookid, br.memberid, " +
            "       b.title   AS book_title, " +
            "       m.surname AS member_surname, m.name AS member_name " +
            "FROM borrow_record br " +
            "LEFT JOIN book   b ON br.bookid   = b.book_id " +
            "LEFT JOIN member m ON br.memberid = m.member_id ";

        String sql = "overdue".equals(filter)
            ? base + "WHERE br.return_date IS NULL AND br.due_date < CURDATE() ORDER BY br.due_date ASC"
            : base + "ORDER BY br.borrow_date DESC";

        try (Connection c = DBConnection.getConnection();
             Statement s  = c.createStatement();
             ResultSet r  = s.executeQuery(sql)) {
            while (r.next()) {
                String surname = r.getString("member_surname");
                String mname   = r.getString("member_name");
                String fullName = ((surname != null ? surname : "") + " "
                                 + (mname   != null ? mname   : "")).trim();

                rentList.add(new BorrowRecord(
                    r.getInt("record_id"),    r.getString("borrow_date"),
                    r.getString("due_date"),  r.getString("return_date"),
                    r.getString("status"),    r.getInt("bookid"),
                    r.getInt("memberid"),
                    r.getString("book_title"),   // номын нэр
                    fullName                     // уншигчийн овог + нэр
                ));
            }
        } catch (Exception e) {
            showAlert("Алдаа", "Түрээс ачаалахад алдаа:\n" + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void btnBorrowBook(ActionEvent event) {
        if (bookList.isEmpty()) {
            showAlert("Боломжгүй", "Бүртгэлтэй ном алга байна.", AlertType.WARNING);
            return;
        }
        if (memberList.isEmpty()) {
            showAlert("Боломжгүй", "Бүртгэлтэй уншигч алга байна.", AlertType.WARNING);
            return;
        }

        Book selBook = tableBook.getSelectionModel().getSelectedItem();

        Dialog<Object[]> dlg = new Dialog<>();
        dlg.setTitle("Ном түрээслэх");
        dlg.setHeaderText("Мэдээлэл оруулна уу:");
        ButtonType ok = new ButtonType("Түрээслэх", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        // ЗАСВАР: ID-ийн оронд нэрээр (бичиж хайдаг ComboBox) сонгоно.
        // ComboBox-д бодит Book/Member объект сонгогддог тул дотооддоо ID нь баталгаатай гарна.
        ComboBox<Book> cbBook = new ComboBox<>(bookList);
        cbBook.setEditable(true);
        cbBook.setPrefWidth(260);
        cbBook.setPromptText("Номын нэрээ бичнэ үү");
        cbBook.setConverter(new StringConverter<Book>() {
            @Override public String toString(Book b) {
                return b == null ? "" : b.getTitle() + " — ISBN " + b.getIsbn();
            }
            @Override public Book fromString(String s) {
                if (s == null || s.isBlank()) return null;
                String q = s.trim().toLowerCase();
                // 1) яг таарсан дэлгэцийн текст эсвэл нэрээр хайх
                for (Book b : bookList) {
                    if (toString(b).toLowerCase().equals(q)
                        || b.getTitle().toLowerCase().equals(q)) return b;
                }
                // 2) хэсэгчилсэн таарал (нэг л таарвал авна)
                Book match = null; int hits = 0;
                for (Book b : bookList) {
                    if (b.getTitle().toLowerCase().contains(q)
                        || b.getIsbn().toLowerCase().contains(q)) { match = b; hits++; }
                }
                return hits == 1 ? match : null;
            }
        });
        if (selBook != null) cbBook.setValue(selBook);

        ComboBox<Member> cbMember = new ComboBox<>(memberList);
        cbMember.setEditable(true);
        cbMember.setPrefWidth(260);
        cbMember.setPromptText("Уншигчийн нэрээ бичнэ үү");
        cbMember.setConverter(new StringConverter<Member>() {
            @Override public String toString(Member m) {
                return m == null ? "" : m.getSurname() + " " + m.getName() + " — " + m.getPhone();
            }
            @Override public Member fromString(String s) {
                if (s == null || s.isBlank()) return null;
                String q = s.trim().toLowerCase();
                for (Member m : memberList) {
                    if (toString(m).toLowerCase().equals(q)
                        || (m.getSurname() + " " + m.getName()).toLowerCase().equals(q)) return m;
                }
                Member match = null; int hits = 0;
                for (Member m : memberList) {
                    if (m.getSurname().toLowerCase().contains(q)
                        || m.getName().toLowerCase().contains(q)
                        || m.getPhone().toLowerCase().contains(q)) { match = m; hits++; }
                }
                return hits == 1 ? match : null;
            }
        });

        TextField fDueDate = new TextField();
        fDueDate.setPromptText("Буцаах огноо (yyyy-MM-dd)");

        GridPane g = new GridPane(); g.setHgap(10); g.setVgap(10);
        g.addRow(0, new Label("Ном:"),          cbBook);
        g.addRow(1, new Label("Уншигч:"),       cbMember);
        g.addRow(2, new Label("Буцаах огноо:"), fDueDate);
        dlg.getDialogPane().setContent(g);

        dlg.setResultConverter(b -> b == ok
            ? new Object[]{ cbBook.getValue(), cbMember.getValue(), fDueDate.getText().trim() }
            : null);

        dlg.showAndWait().ifPresent(vals -> {
            Book   chosenBook   = (vals[0] instanceof Book)   ? (Book) vals[0]   : null;
            Member chosenMember = (vals[1] instanceof Member) ? (Member) vals[1] : null;
            String dueDate      = (String) vals[2];

            if (chosenBook == null) {
                showAlert("Алдаа", "Номоо жагсаалтаас зөв сонгоно уу.", AlertType.ERROR);
                return;
            }
            if (chosenMember == null) {
                showAlert("Алдаа", "Уншигчаа жагсаалтаас зөв сонгоно уу.", AlertType.ERROR);
                return;
            }
            if (dueDate.isEmpty()) {
                showAlert("Алдаа", "Буцаах огноог оруулна уу.", AlertType.ERROR);
                return;
            }

            int bookId   = chosenBook.getId();
            int memberId = chosenMember.getMemberId();

            Connection c = null;
            try {
                c = DBConnection.getConnection();
                c.setAutoCommit(false);

                PreparedStatement upBook = c.prepareStatement(
                    "UPDATE book SET available_qty = available_qty - 1 " +
                    "WHERE book_id=? AND available_qty > 0");
                upBook.setInt(1, bookId);
                if (upBook.executeUpdate() == 0) {
                    c.rollback();
                    showAlert("Боломжгүй", "Ном байхгүй байна.", AlertType.WARNING);
                    return;
                }

                PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO borrow_record (borrow_date, due_date, status, bookid, memberid) " +
                    "VALUES (CURDATE(), ?, 'borrowed', ?, ?)");
                ins.setString(1, dueDate);
                ins.setInt(2, bookId);
                ins.setInt(3, memberId);
                ins.executeUpdate();

                c.commit();
                showAlert("Амжилттай", "Ном түрээслэгдлээ.", AlertType.INFORMATION);
                loadBooks();
                loadRentRecords("all");

            } catch (Exception e) {
                try { if (c != null) c.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
                showAlert("Алдаа", "Түрээслэхэд алдаа:\n" + e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            } finally {
                if (c != null) {
                    try { c.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
                    try { c.close(); }            catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });
    }

    @FXML
    void btnReturnBook(ActionEvent event) {
        BorrowRecord sel = tableRent.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Анхааруулга", "Буцаах түрээсийн бүртгэлийг сонгоно уу.", AlertType.WARNING);
            return;
        }
        if ("returned".equalsIgnoreCase(sel.getStatus())) {
            showAlert("Боломжгүй", "Энэ ном аль хэдийн буцаагдсан байна.", AlertType.WARNING);
            return;
        }

        Alert cf = new Alert(AlertType.CONFIRMATION);
        cf.setHeaderText(null);
        cf.setContentText("Бүртгэл #" + sel.getRecordId() + " буцаах уу?");
        cf.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            Connection c = null;
            try {
                c = DBConnection.getConnection();
                c.setAutoCommit(false);

                // 1. borrow_record шинэчлэх
                PreparedStatement updRec = c.prepareStatement(
                    "UPDATE borrow_record SET return_date=CURDATE(), status='returned' WHERE record_id=?");
                updRec.setInt(1, sel.getRecordId());
                updRec.executeUpdate();

                // 2. available_qty +1 (sel.getBookId()-ийг ашигладаг тул bookId талбар хэвээр хадгалагдсан)
                PreparedStatement updBook = c.prepareStatement(
                    "UPDATE book SET available_qty = available_qty + 1 " +
                    "WHERE book_id=? AND available_qty < quantity");
                updBook.setInt(1, sel.getBookId());
                updBook.executeUpdate();

                c.commit();
                showAlert("Амжилттай", "Ном буцаагдлаа.", AlertType.INFORMATION);
                loadBooks();
                loadRentRecords("all");

            } catch (Exception e) {
                try { if (c != null) c.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
                showAlert("Алдаа", "Буцаахад алдаа:\n" + e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            } finally {
                if (c != null) {
                    try { c.setAutoCommit(true); } catch (Exception ex) { ex.printStackTrace(); }
                    try { c.close(); }            catch (Exception ex) { ex.printStackTrace(); }
                }
            }
        });
    }

    private void showAlert(String title, String content, AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }

    private void clearBookFields() {
        txtBookName.clear(); txtAuthor.clear(); txtIsb.clear(); txtQauntity.clear();
    }

    private void clearMemberFields() {
        txtSurname.clear(); txtName.clear(); txtPhone.clear(); txtEmail.clear();
    }
}