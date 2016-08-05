package javauction.controller;

import javauction.model.AuctionEntity;
import javauction.model.BidEntity;
import javauction.model.CategoryEntity;
import javauction.model.UserEntity;
import javauction.service.AuctionService;
import javauction.service.CategoryService;
import javauction.service.UserService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by gpelelis on 4/7/2016.
 */
@WebServlet(name = "auction")
public class auction extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AuctionService auctionService = new AuctionService();
        CategoryService categoryService = new CategoryService();
        String next_page = "index.jsp";

        if (request.getParameter("action").equals("addNew")){
            // get the user input
            String Name = request.getParameter("name");
            String Description = request.getParameter("description");
            float LowestBid = Float.parseFloat(request.getParameter("lowestBid"));
            String startToday = request.getParameter("startToday");
            int activeDays = Integer.parseInt(request.getParameter("activeDays"));
            String Country = request.getParameter("country");
            String City = request.getParameter("city");
            String instantBuy = request.getParameter("instantBuy");
            String[] categoriesParam = request.getParameterValues("categories");
            /* get userid from session. userid will be sellerid for this specific auction! */
            HttpSession session = request.getSession();
            long userId = (long) session.getAttribute("uid");
            // find out if we can sell this auction instantly
            float buyPrice = -1;
            if (instantBuy.equals("true")){
                buyPrice = Float.parseFloat(request.getParameter("buyPrice"));
            }

            // the auction will start now, so we have to find the current date
            java.sql.Date startDate = null;
            java.sql.Date endDate = null;
            byte isStarted = 0;
            if (startToday.equals("true")){
                java.util.Date currentDate = new java.util.Date(System.currentTimeMillis());
                startDate = new java.sql.Date(currentDate.getTime());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                try {
                    c.setTime(sdf.parse(String.valueOf(startDate)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                c.add(Calendar.DATE, activeDays);  // number of days to add
                endDate = Date.valueOf(sdf.format(c.getTime()));  // dt is now the new date

                isStarted = 1;
            }

            AuctionEntity auction = new AuctionEntity(Name, Description, LowestBid, Country, City, buyPrice, startDate, isStarted, endDate, userId);
            // tell the service to add a new auction
            try {
                // map the auction with the specified categories
                CategoryEntity category;
                Set<CategoryEntity> categories = new HashSet<>();
                for (String cid : categoriesParam){
                    category = categoryService.getCategory(Integer.parseInt(cid));
                    categories.add(category);
                }
                auction.setCategories(categories);

                /* if auction submitted successfully */
                if (auctionService.addAuction(auction)) {
                    request.setAttribute("aid", auction.getAuctionId());
                    next_page = "/auctionSubmit.jsp";
                } else {
                    System.out.println("failure: " + auction);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (request.getParameter("action").equals("activateAuction")){
            response.setContentType("text/html");

            AuctionEntity auction;
            long aid = Long.parseLong(request.getParameter("aid"));

            // retrieve auction's info
            try {
                auctionService.activateAuction(aid);
                auction = auctionService.getAuction(aid);

                /* all categories */
                List categoryLst = categoryService.getAllCategories();
                request.setAttribute("categoryLst", categoryLst);
                /* Auctions selected categories*/
                Set <CategoryEntity> cats = auction.getCategories();
                List<CategoryEntity> usedCategories = new ArrayList<>();
                for (CategoryEntity c : cats){
                    usedCategories.add(new CategoryEntity(c.getCategoryId(), c.getCategoryName()));
                }
                request.setAttribute("usedCategories", usedCategories);

                request.setAttribute("auction", auction);
            } catch (Exception e) {
                e.printStackTrace();
            }
            next_page = "/auctionInfo.jsp";
        } else if (request.getParameter("action").equals("updateAuction")) {
            String name = request.getParameter("name");
            String desc = request.getParameter("description");
            float lowestBid = Float.parseFloat(request.getParameter("lowestBid"));
            float finalPrice = Float.parseFloat(request.getParameter("finalPrice"));
            float buyPrice = Float.parseFloat(request.getParameter("buyPrice"));
            String city = request.getParameter("city");
            String country = request.getParameter("country");
            Date startingDate = Date.valueOf(request.getParameter("startingDate"));
            Date endingDate = Date.valueOf(request.getParameter("endingDate"));
            long aid = Long.parseLong(request.getParameter("aid"));
            String[] categoriesParam = request.getParameterValues("categories");

            try {
                // map the auction with the specified categories
                Set<CategoryEntity> categories = null;
                if (categoriesParam != null) {
                    CategoryEntity category;
                    categories = new HashSet<>();
                    for (String cid : categoriesParam) {
                        category = categoryService.getCategory(Integer.parseInt(cid));
                        categories.add(category);
                    }
                }
                auctionService.updateAuction(categories, aid, name, desc, lowestBid, finalPrice, buyPrice, city, country, startingDate, endingDate);

                AuctionEntity auction = auctionService.getAuction(aid);
                request.setAttribute("auction", auction);
                /* all categories */
                List categoryLst = categoryService.getAllCategories();
                request.setAttribute("categoryLst", categoryLst);
                /* Auctions selected categories*/
                Set <CategoryEntity> cats = auction.getCategories();
                List<CategoryEntity> usedCategories = new ArrayList<>();
                for (CategoryEntity c : cats){
                    usedCategories.add(new CategoryEntity(c.getCategoryId(), c.getCategoryName()));
                }
                request.setAttribute("usedCategories", usedCategories);
                /* get the highest bid */
                Set <BidEntity> allBids = auction.getBids();
                List<BidEntity> bidLst = new ArrayList<>();
                List<UserEntity> biddersLst = new ArrayList<>();
                UserService userService = new UserService();
                for (BidEntity b : allBids){
                    bidLst.add(b);
                    biddersLst.add(userService.getUser(b.getBidderId()));
                }
                request.setAttribute("bidLst", bidLst);
                request.setAttribute("biddersLst", biddersLst);
                /* if auction has ended */
                Date currentDate = new Date(System.currentTimeMillis());
                request.setAttribute("isEnded", auction.getEndingDate().before(currentDate));

                next_page = "/auctionInfo.jsp";
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (request.getParameter("action").equals("deleteAuction")) {
            response.setContentType("text/html");

            try {
                long aid = Long.parseLong(request.getParameter("aid"));
                auctionService.deleteAuction(aid);
                
//                TODO: FIX DELETE, doesn't delete from database!

                next_page = "/homepage.jsp";
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (request.getParameter("action").equals("bidAuction")) {
            float amount = Float.parseFloat(request.getParameter("bid"));
            long aid = Long.parseLong(request.getParameter("aid"));
            HttpSession session = request.getSession();
            long uid = (long) session.getAttribute("uid");

            BidEntity bid = new BidEntity(uid, aid, amount);
            auctionService.bidAuction(bid);

            AuctionEntity auction = auctionService.getAuction(aid);
            request.setAttribute("auction", auction);
            /* Auctions selected categories*/
            Set <CategoryEntity> cats = auction.getCategories();
            List<CategoryEntity> usedCategories = new ArrayList<>();
            for (CategoryEntity c : cats){
                usedCategories.add(new CategoryEntity(c.getCategoryId(), c.getCategoryName()));
            }
            request.setAttribute("usedCategories", usedCategories);
            /* get the highest bid */
            Set <BidEntity> allBids = auction.getBids();
            List<BidEntity> bidLst = new ArrayList<>();
            List<UserEntity> biddersLst = new ArrayList<>();
            UserService userService = new UserService();
            for (BidEntity b : allBids){
                bidLst.add(b);
                biddersLst.add(userService.getUser(b.getBidderId()));
            }
            request.setAttribute("bidLst", bidLst);
            request.setAttribute("biddersLst", biddersLst);
            /* if auction has ended */
            Date currentDate = new Date(System.currentTimeMillis());
            request.setAttribute("isEnded", auction.getEndingDate().before(currentDate));
            next_page = "/auctionInfo.jsp";
        }

        RequestDispatcher view = request.getRequestDispatcher(next_page);
        view.forward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String next_page = null;
        String param = request.getParameter("action");
        AuctionService auctionService = new AuctionService();
        HttpSession session = request.getSession();
        CategoryService categoryService = new CategoryService();
        List categoryLst = categoryService.getAllCategories();
        switch (param) {
            case "getAllAuctions": /* get all actions with sellerId = uid (from session) */
                long userID = (long) session.getAttribute("uid");
                List auctionLst = auctionService.getAllAuctions(userID, false);
                request.setAttribute("auctionLst", auctionLst);
                next_page = "/listAuctions.jsp";
                break;
            case "getAllActiveAuctions": /* get all active auctions, all sellers */
                request.setAttribute("auctionLst", auctionService.getAllAuctions(-1, true));
                next_page = "/listAuctions.jsp";
                break;
            case "newAuction": /* gather all categories to display on jsp */
                request.setAttribute("categoryLst", categoryLst);
                next_page = "/newAuction.jsp";
                break;
            case "getAnAuction": /* get an auction with auctionId = aid */
                long aid = Long.parseLong(request.getParameter("aid"));
                AuctionEntity auction = auctionService.getAuction(aid);
                // check if session.uid == link-parameters.uid (seller)
                long sid_link = Long.parseLong(request.getParameter("uid"));
                long uid = (long) session.getAttribute("uid");
                assert(sid_link == uid);
                /* get seller id for the auction */
                long sid = auction.getSelledId();
                session.setAttribute("isSeller", sid == sid_link);
                /* all categories */
                request.setAttribute("categoryLst", categoryLst);
                /* Auctions selected categories*/
                Set <CategoryEntity> cats = auction.getCategories();
                List<CategoryEntity> usedCategories = new ArrayList<>();
                for (CategoryEntity c : cats){
                    usedCategories.add(new CategoryEntity(c.getCategoryId(), c.getCategoryName()));
                }
                request.setAttribute("usedCategories", usedCategories);
                /* get the highest bid */
                Set <BidEntity> allBids = auction.getBids();
                List<BidEntity> bidLst = new ArrayList<>();
                List<UserEntity> biddersLst = new ArrayList<>();
                UserService userService = new UserService();
                for (BidEntity b : allBids){
                    bidLst.add(b);
                    biddersLst.add(userService.getUser(b.getBidderId()));
                }
                request.setAttribute("bidLst", bidLst);
                request.setAttribute("biddersLst", biddersLst);
                /* if auction has ended */
                Date currentDate = new Date(System.currentTimeMillis());
                request.setAttribute("isEnded", auction.getEndingDate().before(currentDate));
                request.setAttribute("auction", auction);
                next_page = "/auctionInfo.jsp";
                break;
        }

        RequestDispatcher view = request.getRequestDispatcher(next_page);
        view.forward(request, response);
    }

}
