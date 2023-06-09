package com.bmg.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bmg.beans.Admin;
import com.bmg.beans.Booking;
import com.bmg.beans.Facility;
import com.bmg.beans.Hosting;
import com.bmg.beans.Inbox;
import com.bmg.beans.User;
import com.bmg.compositekey.BookKey;
import com.bmg.compositekey.HostKey;
import com.bmg.factory.BMGServiceFactory;
import com.bmg.service.IBMGService;

@WebServlet("/controller/*")
public class ControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doProcess(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doProcess(request, response);
	}

	protected void doProcess(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uri = request.getRequestURI();
		HttpSession session = request.getSession();
		RequestDispatcher rq = null;
		User user = null;
		Admin admin = null;
		String status = null;
		ServletContext context = null;
		IBMGService service = BMGServiceFactory.getBMGService();

		if (uri.endsWith("getregisterpage")) {
			if (request.getParameter("signupType").equalsIgnoreCase("manager")) {
				request.setAttribute("name", request.getParameter("name"));
				request.setAttribute("signupType", request.getParameter("signupType"));
				rq = request.getRequestDispatcher("../managerregister.jsp");
				rq.forward(request, response);

			} else if (request.getParameter("signupType").equalsIgnoreCase("player")) {
				request.setAttribute("name", request.getParameter("name"));
				request.setAttribute("signupType", request.getParameter("signupType"));
				rq = request.getRequestDispatcher("../playerregister.jsp");
				rq.forward(request, response);

			}
		}
		if (uri.endsWith("register")) {
			if (request.getParameter("signupType").equalsIgnoreCase("player")) {
				user = new User();
				user.setName(request.getParameter("name"));
				user.setEmail(request.getParameter("mail"));
				user.setpNumber(request.getParameter("phonenum"));
				user.setPassword(request.getParameter("password"));
				status = service.registerUser(user);
			} else if (request.getParameter("signupType").equalsIgnoreCase("manager")) {
				admin = new Admin();
				admin.setName(request.getParameter("name"));
				admin.setEmail(request.getParameter("mail"));
				admin.setpNumber(request.getParameter("phonenum"));
				admin.setPassword(request.getParameter("password"));
				status = service.registerAdmin(admin);

			}
			if (status.equalsIgnoreCase("success")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "success");
				rq.forward(request, response);
			} else if (status.equalsIgnoreCase("error")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "error");
				rq.forward(request, response);
			}

		}
		if (uri.endsWith("login")) {
			String mail = request.getParameter("usermail");
			String password = request.getParameter("password");
			String userType = request.getParameter("loginType");
			if (userType.equalsIgnoreCase("player")) {
				if (service.verifyLogin(mail, password, userType)) {
					session.setAttribute("mail", mail);

					String url = response.encodeURL("../playerInterface.jsp");
				
					response.sendRedirect(url);
				} else {
					response.sendRedirect("http://localhost:9999/BookMyGameApp/login.html");

				}
			} else if (userType.equalsIgnoreCase("manager")) {
				if (service.verifyLogin(mail, password, userType)) {
					session.setAttribute("mail", mail);
					ArrayList<Facility> myFacilities = service.getAllFacilities(mail);
					session.setAttribute("facilities", myFacilities);
					String url = response.encodeURL("../adminInterface.jsp");
					response.sendRedirect(url);
				} else {
					response.sendRedirect("http://localhost:9999/BookMyGameApp/login.html");

				}

			}

		}
		if (uri.endsWith("checkfeed")) {
			ArrayList<Facility> facilities = service.checkfeed();
			if (facilities != null) {
				request.setAttribute("facilities", facilities);
				rq = request.getRequestDispatcher("../checkfeed.jsp");
				rq.forward(request, response);

			} else {
				PrintWriter out = response.getWriter();
				out.println("No Bookings Available");
			}
		}
		if (uri.endsWith("myfacility")) {
			ArrayList<Facility> myFacilities = service.getAllFacilities((String) session.getAttribute("mail"));
			session.setAttribute("facilities", myFacilities);
			String url = response.encodeURL("../adminInterface.jsp");
			response.sendRedirect(url);
		}
		if (uri.endsWith("book")) {
			request.setAttribute("fname", request.getParameter("fname"));
			rq = request.getRequestDispatcher("../booking.jsp");
			rq.forward(request, response);
		}
		if (uri.endsWith("logout")) {
			session.removeAttribute("mail");
			session.removeAttribute("facilities");
			session.invalidate();
			response.sendRedirect("../index.html");

		}
		if (uri.endsWith("registerfacility")) {
			Facility facility = new Facility();
			facility.setFname(request.getParameter("fname"));
			facility.setFowner(request.getParameter("fowner"));
			facility.setFphone(request.getParameter("fphone"));
			facility.setFsport(request.getParameter("fsport"));
			facility.setFloc(request.getParameter("floc"));
			facility.setFmail(request.getParameter("fmail"));
			facility.setFomail(request.getParameter("fomail"));
			status = service.registerFacility(facility);
			if (status.equalsIgnoreCase("success")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "rsuccess");
				rq.forward(request, response);
			} else if (status.equalsIgnoreCase("facerror")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "facerror");
				rq.forward(request, response);

			} else if (status.equalsIgnoreCase("error")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "rerror");
				rq.forward(request, response);

			}
			else {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "rfailed");
				rq.forward(request, response);
			}
		}
		if (uri.endsWith("modifyFacility")) {
			if (request.getParameter("operation").equalsIgnoreCase("update")) {
				String name = request.getParameter("fname");
				request.setAttribute("facility", service.getFacility(name));
				request.setAttribute("facilityname", name);
				rq = request.getRequestDispatcher("../updatefacility.jsp");
				rq.forward(request, response);
			} else if (request.getParameter("operation").equalsIgnoreCase("delete")) {
				status = service.deleteFacitlity(request.getParameter("fname"));
				if (status.equalsIgnoreCase("success")) {

					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "dsuccess");
					rq.forward(request, response);
				} else if (status.equalsIgnoreCase("error")) {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "derror");
					rq.forward(request, response);

				} else {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "dfailed");
					rq.forward(request, response);
				}

			}
		}
		if(uri.endsWith("adminprofile")) {
			 admin = service.getAdminInfo((String) session.getAttribute("mail"));
			 if(admin==null) { 
				  response.getWriter().println("Sorry Error");
			 }
			 rq = request.getRequestDispatcher("../profile.jsp");
			 request.setAttribute("type", "admin");
			 request.setAttribute("admin", admin);
			 rq.forward(request, response);
		}
		if(uri.endsWith("playerprofile")) {
			 user = service.getUserInfo((String) session.getAttribute("mail"));
			 if(user==null) { 
				  response.getWriter().println("Sorry Error");
			 }
			 rq = request.getRequestDispatcher("../profile.jsp");
			 request.setAttribute("type", "user");
			 request.setAttribute("user", user);
			 rq.forward(request, response);
		}
		if (uri.endsWith("updatefacility")) {
			String name = request.getParameter("facilityname");
			Facility facility = new Facility();
			facility.setFname(name);
			facility.setFowner(request.getParameter("fowner"));
			facility.setFphone(request.getParameter("fphone"));
			facility.setFsport(request.getParameter("fsport"));
			facility.setFloc(request.getParameter("floc"));
			facility.setFmail(request.getParameter("fmail"));
			facility.setFomail((String) session.getAttribute("mail"));
			status = service.updateFaciltiy(facility);
			if (status.equalsIgnoreCase("success")) {
				System.out.println(request.getRequestURI());
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "usuccess");
				rq.forward(request, response);
			} else if (status.equalsIgnoreCase("error")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "uerror");
				rq.forward(request, response);

			} else {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "ufailed");
				rq.forward(request, response);
			}
		}
		if (uri.endsWith("bookfacility")) {
			BookKey bookKey = new BookKey();
			Booking booking = new Booking();
			bookKey.setFname(request.getParameter("fname"));
			bookKey.setBookDate(LocalDate.parse(request.getParameter("bdate")));
			booking.setBookKey(bookKey);
			booking.setUserMail((String) session.getAttribute("mail"));
			booking.setBookType(request.getParameter("btype"));
			status = service.bookFacility(booking);
			if (status.equalsIgnoreCase("success")) {
				System.out.println(request.getRequestURI());
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "bsuccess");
				rq.forward(request, response);
			} else if (status.equalsIgnoreCase("error")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "berror");
				rq.forward(request, response);

			} else if (status.equalsIgnoreCase("integrityerror")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "bierror");
				rq.forward(request, response);

			} else {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "bfailed");
				rq.forward(request, response);
			}

		}
		if (uri.endsWith("mybookings")) {
			String mail = (String) session.getAttribute("mail");
			ArrayList<Booking> myBookings = service.getMyBookings(mail);
			if (myBookings.isEmpty()) {
				response.getWriter().println("NO BOOKINGS!!");
			} else {
				rq = request.getRequestDispatcher("../mybookings.jsp");
				request.setAttribute("bookings", myBookings);
				rq.forward(request, response);
			}

		}
		if (uri.endsWith("cancelbooking")) {
			String facilityName = request.getParameter("fname");
			LocalDate bdate = LocalDate.parse(request.getParameter("bdate"));
			status = service.cancelFacility(facilityName, bdate);
			if (status.equalsIgnoreCase("success")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "csuccess");
				rq.forward(request, response);
			} else if (status.equalsIgnoreCase("error")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "cerror");
				rq.forward(request, response);

			} else {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "cfailed");
				rq.forward(request, response);
			}

		}
		if (uri.endsWith("admincancelbookings")) {
			String facilityName = request.getParameter("fname");
			LocalDate bdate = LocalDate.parse(request.getParameter("bdate"));
			String reason = request.getParameter("reason");
			String mail = request.getParameter("userMail");
			status = service.adminCancelFacility(facilityName, bdate);
			if (status.equalsIgnoreCase("success")) {
				Inbox inbox = new Inbox();
				inbox.setMessage(reason);
				inbox.setUserMail(mail);
				status = service.updateInbox(inbox);
				if (status.equalsIgnoreCase("success")) {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "isuccess");
					rq.forward(request, response);

				}

			} else if (status.equalsIgnoreCase("error")) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "cerror");
				rq.forward(request, response);
			} else {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "cfailed");
				rq.forward(request, response);
			}

		}
		if (uri.endsWith("checkbookings")) {
			String mail = (String) session.getAttribute("mail");
			ArrayList<Booking> bookings = service.getBookings(mail);
			if (bookings.isEmpty()) {
				response.getWriter().println("NO BOOKINGS!!!");
			} else {
				rq = request.getRequestDispatcher("../bookedFacilities.jsp");
				request.setAttribute("booked", bookings);
				rq.forward(request, response);
			}
		}
		if (uri.endsWith("getcancelpage")) {
			rq = request.getRequestDispatcher("../cancelbooking.jsp");
			request.setAttribute("fname", request.getParameter("fname"));
			request.setAttribute("bookdate", request.getParameter("bdate"));
			request.setAttribute("usermail", request.getParameter("userMail"));
			rq.forward(request, response);
		}
		if (uri.endsWith("inbox")) {
			ArrayList<Inbox> messages = service.getMessages((String) session.getAttribute("mail"));
			if (messages.isEmpty()) {
				response.getWriter().print("no messages yet!!");
			} else {
				rq = request.getRequestDispatcher("../inbox.jsp");
				request.setAttribute("messages", messages);
				rq.forward(request, response);
			}
		}
		if (uri.endsWith("hosting")) {
			String mail = (String) session.getAttribute("mail");
			ArrayList<Booking> myhostBookings = service.getHostings(mail);
			if (myhostBookings.isEmpty()) {

				request.setAttribute("youhostings", "no");
			}
			ArrayList<Booking> hostBookings = service.getAllHostings(mail);
			if (hostBookings.isEmpty()) {

				request.setAttribute("hosting", "no");
			}
			ArrayList<Hosting> hostingRequests = service.getHostingRequests(mail);
			if (hostingRequests.isEmpty()) {

				request.setAttribute("hostingreq", "no");
			}
			request.setAttribute("yourhostings", myhostBookings);
			request.setAttribute("hostings", hostBookings);
			request.setAttribute("hostingrequests", hostingRequests);

			rq = request.getRequestDispatcher("../hostings.jsp");
			rq.forward(request, response);
		}
		if (uri.endsWith("joinhost")) {
			Hosting hosting = new Hosting();
			HostKey hostKey = new HostKey();
			hostKey.setFname(request.getParameter("fname"));
			hostKey.setBdate(LocalDate.parse(request.getParameter("bdate")));
			hostKey.setUserMail((String) session.getAttribute("mail"));
			hosting.setHostKey(hostKey);
			hosting.setHostMail(request.getParameter("hostmail"));
			hosting.setStatus("pending");
			status = service.sendJoinRequest(hosting);
			if ("success".equalsIgnoreCase(status)) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "reqsuccess");
				rq.forward(request, response);
			} else if ("already".equalsIgnoreCase(status)) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "reqalready");
				rq.forward(request, response);
			} else if ("error".equalsIgnoreCase(status)) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "reqerror");
				rq.forward(request, response);
			} else if ("failed".equalsIgnoreCase(status)) {
				rq = request.getRequestDispatcher("../result.jsp");
				context = getServletContext();
				context.setAttribute("result", "reqfailed");
				rq.forward(request, response);
			}
		}
		if (uri.endsWith("requestaccept")) {
			Hosting hosting = new Hosting();
			HostKey hostKey = new HostKey();
			String userMail = request.getParameter("usermail");
			String facilityName = request.getParameter("fname");
			LocalDate date = LocalDate.parse(request.getParameter("bdate"));
			hostKey.setBdate(date);
			hostKey.setFname(facilityName);
			hostKey.setUserMail(userMail);
			hosting.setHostKey(hostKey);
			hosting.setStatus(request.getParameter("joinrequest"));
			status = service.acceptJoinRequest(hosting);
			if (status.equalsIgnoreCase("accepted")) {
				Inbox inbox = new Inbox();
				String message = "Your Request for joining tournament at " + facilityName + "on " + date + "is Accepted";
				inbox.setUserMail(userMail);
				inbox.setMessage(message);
				status = service.updateInbox(inbox);
				if (status.equalsIgnoreCase("success")) {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "rasuccess");
					rq.forward(request, response);
				} else if (status.equalsIgnoreCase("error")) {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "raerror");
					rq.forward(request, response);
				} else {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "rafailed");
					rq.forward(request, response);
				}
			} else if (status.equalsIgnoreCase("declined")) {
				Inbox inbox = new Inbox();
				String message = "Your Request for joining tournament at " + facilityName + "on " + date + "is Declined";
				inbox.setUserMail(userMail);
				inbox.setMessage(message);
				System.out.print(inbox.getMessage());
				status = service.updateInbox(inbox);
				if (status.equalsIgnoreCase("success")) {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "radsuccess");
					rq.forward(request, response);
				} else if (status.equalsIgnoreCase("error")) {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "raerror");
					rq.forward(request, response);
				} else {
					rq = request.getRequestDispatcher("../result.jsp");
					context = getServletContext();
					context.setAttribute("result", "rafailed");
					rq.forward(request, response);
				}

			}

		}

	}
}
