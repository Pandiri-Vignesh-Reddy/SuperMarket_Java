import java.sql.*;
import java.util.*;

public class Supermarket {
    private static void generateSalesReport(Connection connection) throws SQLException {
        System.out.println("\n********** Daily Sales Report **********");
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT DATE(sale_date) AS date, SUM(sold_quantity) AS total_quantity, SUM(total_sales) AS total_amount FROM sales GROUP BY DATE(sale_date)");
        System.out.println("Date\t\t\tTotal Quantity\tTotal Amount");
        while (rs.next()) {
            java.util.Date date = rs.getDate("date");
            int totalQuantity = rs.getInt("total_quantity");
            double totalAmount = rs.getDouble("total_amount");
            System.out.println(date + "\t\t" + totalQuantity + "\t\t" + totalAmount);
        }
        rs.close();
        stmt.close();
    }
    private static void displayDiscountProducts(Connection connection) throws SQLException {
        System.out.println("\n********** Discount Products **********");
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM products WHERE discount_in_percent > 0");
        System.out.println("Product Name\tPrice\tDiscount %");
        while (rs.next()) {
            System.out.println(rs.getString(1) + "\t" + rs.getDouble(2) +"\t" + rs.getDouble(4)+"%");
        }
        rs.close();
        stmt.close();
    }
    public static void main(String[] args)
    {
    try {
            Scanner sc=new Scanner(System.in);
            Connection c=DriverManager.getConnection("jdbc:mysql://localhost:3306/supermarket","root", "scabmoses345");
            int choice;
            System.out.println("\n\t\t***********Supermarket Management System*****************");
            do {
                System.out.println("\n1. Display products");
                System.out.println("2. Sell product");
                System.out.println("3. Add product");
                System.out.println("4. Update product price");
                System.out.println("5. Update product quantity");
                System.out.println("6. Generate sales report");
                System.out.println("7. Show discount products");
                System.out.println("8. Exit");
                System.out.print("Enter your choice: ");
                choice = sc.nextInt();   
                switch(choice)
                {
                    case 1:
                        Statement c1=c.createStatement();
                        ResultSet rs=c1.executeQuery("select * from products");
                        ResultSetMetaData col=rs.getMetaData();
                        int columnCount = col.getColumnCount();
                        int[] columnWidths = new int[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        columnWidths[i - 1] = Math.max(col.getColumnName(i).length(), 25); // Minimum width of 15 characters
                    }
                    // Print column headers
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(String.format("%-" + columnWidths[i - 1] + "s", col.getColumnName(i)));
                    }
                    System.out.println();
                    // Print data rows
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.print(String.format("%-" + columnWidths[i - 1] + "s", rs.getObject(i)));
                        }
                        System.out.println();
                    }
                        rs.close();
                        c1.close();
                        break;
                    case 2:
                        System.out.print("Enter the name of the product: ");
                        String name = sc.next();
                        System.out.print("Enter the quantity to sell: ");
                        int quan = sc.nextInt();
                        Statement c2=c.createStatement();
                        ResultSet r2=c2.executeQuery("select * from products");
                        boolean productFound=false;
                        while(r2.next()){
                        if (name != null && name.equalsIgnoreCase(r2.getString(1))) {
                            productFound=true;
                            if(r2.getInt("Quantity")<quan)
                            {
                                System.out.println("Insufficient quantity of " + name + " available.");
                                break;
                            }
                            else{
                                System.out.print("Enter the sale date (YYYY-MM-DD): ");
                                String saleDateStr = sc.next();
                                java.sql.Date sqlDate = java.sql.Date.valueOf(saleDateStr);
                                double totalSales = r2.getDouble(2)* quan * (1 - (r2.getDouble(4) / 100));
                                PreparedStatement insertSales = c.prepareStatement("INSERT INTO sales (product_name, sold_quantity, total_sales, sale_date) VALUES (?, ?, ?, ?)");
                                insertSales.setString(1, name);
                                insertSales.setInt(2, quan);
                                insertSales.setDouble(3, totalSales);
                                insertSales.setDate(4, sqlDate);
                                insertSales.executeUpdate();
                                System.out.println(quan + " units of " + name + " sold successfully.");
                                CallableStatement p2=c.prepareCall("{call sellProduct(?,?)}");
                                p2.setString(1, name);
                                p2.setInt(2,quan);
                                int res=p2.executeUpdate();
                                System.out.println(res + " Row(s) affected");
                                break;
                                }
                        }
                        }
                        if(!productFound){
                        System.out.println("Product "+ name+ " not found .\nPlease search for other products\n");}
                        break;
                    case 3:
                        System.out.print("Enter the name of the product: ");
                        String addname = sc.next();
                        System.out.print("Enter the price of the product: ");
                        double price = sc.nextDouble();
                        System.out.print("Enter the quantity of the product: ");
                        int quantity = sc.nextInt();
                        System.out.print("Enter the discount for this item(if any) else 0: ");
                        double discount=sc.nextDouble();
                        Statement c3=c.createStatement();
                        ResultSet r3=c3.executeQuery("select name from products");
                        int flag=0;
                        while(r3.next()){
                        String s=r3.getString("name");
                        if(s.equalsIgnoreCase(addname))
                        {
                            flag=1;
                            break;
                        }
                        }
                        if(flag==1){
                            System.out.println("The product "+addname+" is already available in the list .\nUpdating the product with new price quantity and discount.\n");
                            CallableStatement p2=c.prepareCall("{call updateproduct(?,?,?,?)}");
                                p2.setString(1, addname);
                                p2.setDouble(2,price);
                                p2.setInt(3,quantity);
                                p2.setDouble(4,discount);
                                
                                int res=p2.executeUpdate();
                                System.out.println(res + "Row(s) affected");
                                System.out.println("Product " + addname + " added successfully.");
                        }
                        else{
                            PreparedStatement p3=c.prepareStatement("insert into products values(?,?,?,?)");
                            p3.setString(1,addname);
                            p3.setDouble(2,price);
                            p3.setInt(3,quantity);
                            p3.setDouble(4,discount);
                            int res=p3.executeUpdate();
                            System.out.println(res + "Row(s) affected");
                            System.out.println("Product " + addname + " added successfully.");
                    
                        }
                        break;
                    case 4:
                        System.out.print("Enter the name of the product: ");
                        name = sc.next();
                        System.out.print("Enter the Price to update: ");
                        double price1=sc.nextDouble();
                        Statement c4=c.createStatement();
                        ResultSet r4=c4.executeQuery("select * from products");
                        productFound=false;
                        while(r4.next()){
                        if (name != null && name.equalsIgnoreCase(r4.getString(1))) {
                            productFound=true;
                            PreparedStatement p4=c.prepareStatement("update products set price=? where name=?");
                            p4.setDouble(1,price1);
                            p4.setString(2,name);
                            int res=p4.executeUpdate();
                            System.out.println(res + "Row(s) affected");
                            System.out.println("Product " + name + " updated successfully.");
                        }
                        }
                        if(productFound==false)
                        System.out.println("Product not found !");
                        break;
                    case 5:
                        System.out.print("Enter the name of the product: ");
                        name = sc.next();
                        System.out.print("Enter the Quantity to update: ");
                        int q=sc.nextInt();
                        Statement c5=c.createStatement();
                        ResultSet r5=c5.executeQuery("select * from products");
                        productFound=false;
                        while(r5.next()){
                        if (name != null && name.equalsIgnoreCase(r5.getString(1))) {
                            productFound=true;
                            CallableStatement p2=c.prepareCall("{call updateproduct(?,?,?,?)}");
                            p2.setString(1,name);
                            p2.setDouble(2,r5.getDouble(2));
                            p2.setInt(3,(q-r5.getInt(3)));
                            p2.setDouble(4,r5.getDouble(4));
                            int res=p2.executeUpdate();
                            System.out.println(res + "Row(s) affected");
                            System.out.println("Product " + name + " updated successfully.");
                        }
                        }
                        if(productFound==false)
                        System.out.println("Product not found !");
                        break;
                    case 6:
                        generateSalesReport(c);
                        break;
                    case 7:
                        displayDiscountProducts(c);
                        break;
                    case 8:
                        System.out.println("Exiting the program. Thank you!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }while(choice!=8);
            c.close();
        } 
    catch (Exception e) {
        e.printStackTrace();
    }
    }
}
