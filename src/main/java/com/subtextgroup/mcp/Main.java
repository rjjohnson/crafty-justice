package com.subtextgroup.mcp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	FileConfiguration config = null;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmdName = command.getName();
		if("reportuser".equalsIgnoreCase(cmdName)) {
			return reportUser(sender, command, label, args);
		} else if("reportperson".equalsIgnoreCase(cmdName)) {
			return reportPerson(sender, command, label, args);
		} else if("listreports".equalsIgnoreCase(cmdName)) {
			return listReports(sender, command, label, args);
		} else if("clearreports".equalsIgnoreCase(cmdName)) {
			return clearReports(sender, command, label, args);
		} else if("retractreport".equalsIgnoreCase(cmdName)) {
			return retractReport(sender, command, label, args);
		}
		return false;
	}
	private boolean retractReport(CommandSender sender, Command command, String label, String[] args) {
		List<Report> reports = getAllReports();
		
		Collections.sort(reports, new Comparator<Report>() {
			@Override
			public int compare(Report o1, Report o2) {
				return o2.getReportDate().compareTo(o1.getReportDate());
			}
		});
		Iterator<Report> iter = reports.iterator();
		while(iter.hasNext()) {
			if(sender.getName().equalsIgnoreCase(iter.next().getReporterUsername())) {
				iter.remove();
				config.set("reports", reports);
				saveConfig();
				sender.sendMessage("Retracted your most recent report.");
				return true;
			}
		}
		sender.sendMessage("No reports found to retract.");
		return true;
	}
	private boolean clearReports(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 1) {
			return false;
		} else if(args.length == 1) {
			List<Report> reports = getAllReports();
			Iterator<Report> iter = reports.iterator();
			int count = 0;
			while(iter.hasNext()) {
				if(args[0].equalsIgnoreCase(iter.next().getOffenderUsername())) {
					iter.remove();
					count++;
				}
			}
			config.set("reports", reports);
			saveConfig();
			sender.sendMessage("Cleared " + count + " reports about " + args[0]);
			return true;
		} else {
			config.set("reports", new ArrayList<>());
			saveConfig();
			sender.sendMessage("All reports cleared");
			return true;
		}
		
	}
	private List<Report> getAllReports() {
		return (List<Report>) config.getList("reports", new ArrayList<>());
	}
	private boolean listReports(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2) {
			return false;
		}
		
		String queryType = args[0].toLowerCase();
		List<Report> reports = getAllReports();
		String response = "";
		if("o-user".equals(queryType)) {
			int ctr = 0;
			for(Report report : reports) {
				if(report.getOffenderUsername().equalsIgnoreCase(args[1].trim())) {
					response += (ctr % 2 == 0 ? ChatColor.BLUE : ChatColor.GOLD) + report.toMessageString() + ChatColor.RESET + "\n";
					ctr++;
				}
			}
		} else if("o-name".equals(queryType)) {
			int ctr = 0;
			for(Report report : reports) {
				if(args[1].trim().equalsIgnoreCase(report.getOffenderPersonName())) {
					response += (ctr % 2 == 0 ? ChatColor.BLUE : ChatColor.GOLD) + report.toMessageString() + ChatColor.RESET + "\n";
					ctr++;
				}
			}
		} else if("r-user".equals(queryType)) {
			int ctr = 0;
			for(Report report : reports) {
				if(report.getReporterUsername().equalsIgnoreCase(args[1].trim())) {
					response += (ctr % 2 == 0 ? ChatColor.BLUE : ChatColor.GOLD) + report.toMessageString() + ChatColor.RESET + "\n";
					ctr++;
				}
			}
		} else if("date-range".equals(queryType)) {
			if(args.length < 3) {
				return false;
			}
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			try {
				Date fromDate = dateFormat.parse(args[1]);
				Date toDate = dateFormat.parse(args[2]);
				int ctr = 0;
				for(Report report : reports) {
					if(dateBetweenInclusive(report.getReportDate(), fromDate, toDate)) {
						response += (ctr % 2 == 0 ? ChatColor.BLUE : ChatColor.GOLD) + report.toMessageString() + ChatColor.RESET + "\n";
						ctr++;
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
		
		sender.sendMessage("Reports:\n" + response);
		return true;
	}
	private boolean reportUser(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 2) {
			return false;
		}
		Player reporter = getServer().getPlayer(sender.getName());
		Player offenderPlayer = getServer().getPlayer(args[0]);
		if(offenderPlayer == null) {
			sender.sendMessage(ChatColor.YELLOW + args[0] + " is not a valid player name or is not online right now" + ChatColor.RESET);
			return false;
		}
		String reason = "";
		for(int i = 1; i < args.length; i++) {
			reason += args[i] + " ";
		}
		reason = reason.trim();
		getServer().broadcastMessage(sender.getName() + " reported " + offenderPlayer.getName() + " for: \"" + reason + "\"");
		Report report = new Report(reporter, offenderPlayer, reason, new Date());
		List<Report> reports = getAllReports();
		reports.add(report);
		config.set("reports", reports);
		saveConfig();
		return true;	
	}
	private boolean reportPerson(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 3) {
			return false;
		}
		Player reporter = getServer().getPlayer(sender.getName());
		Player offenderPlayer = getServer().getPlayer(args[0]);
		if(offenderPlayer == null) {
			sender.sendMessage(ChatColor.YELLOW + args[0] + " is not a valid player name or is not online right now");
			return false;
		}
		String offenderPersonName = args[1];
		String reason = "";
		for(int i = 2; i < args.length; i++) {
			reason += args[i] + " ";
		}
		reason = reason.trim();
		getServer().broadcastMessage(sender.getName() + " reported " + offenderPersonName + " [" + offenderPlayer.getName() + "] for: \"" + reason + "\"");
		Report report = new Report(reporter, offenderPlayer, offenderPersonName, reason, new Date());
		List<Report> reports = getAllReports();
		reports.add(report);
		config.set("reports", reports);
		saveConfig();
		return true;	
	}
	private static boolean dateBetweenInclusive(Date date, Date start, Date end) {
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(start);
		startCalendar.add(Calendar.DAY_OF_YEAR, -1);
		
		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(end);
		endCalendar.add(Calendar.DAY_OF_YEAR, 1);
		return date.after(startCalendar.getTime()) && date.before(endCalendar.getTime());
		
	}
	@Override
	public void onDisable() {
	
	}

	@Override
	public void onEnable() {
		ConfigurationSerialization.registerClass(Report.class);
		getServer().broadcastMessage("CraftyJustice enabled!");
		config = getConfig();
	}

	
}
