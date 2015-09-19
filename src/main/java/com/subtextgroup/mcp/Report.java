package com.subtextgroup.mcp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
@SerializableAs("report")
public class Report implements ConfigurationSerializable {
	private static final long serialVersionUID = 1L;
	private String reporterUsername;
	private String reporterUUID;
	private String offenderUsername;
	private String offenderUUID;
	private Date reportDate;
	private String reason;

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public Report() {
		
	}
	public Report(Player reporter, Player offender, String reason, Date reportDate) {
		if(reporter != null) {
			this.reporterUsername = reporter.getName();
			this.reporterUUID = reporter.getUniqueId().toString();
		} else {
			this.reporterUsername = "Console";
		}
		 
		this.offenderUsername = offender.getName();
		this.offenderUUID = offender.getUniqueId().toString();
		this.reportDate = reportDate;
		this.reason = reason;
	}
	public Report(Map<String, String> map) {
		this.reporterUsername = map.get("reporterUsername");
		this.reporterUUID = map.get("reporterUUID");
		this.offenderUsername = map.get("offenderUsername");
		this.offenderUUID = map.get("offenderUUID");
		try {
			this.reportDate = dateFormat.parse(map.get("reportDate"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.reason = map.get("reason");
	}
	public String getReporterUsername() {
		return reporterUsername;
	}
	public void setReporterUsername(String reporterUsername) {
		this.reporterUsername = reporterUsername;
	}
	public String getReporterUUID() {
		return reporterUUID;
	}
	public void setReporterUUID(String reporterUUID) {
		this.reporterUUID = reporterUUID;
	}
	public String getOffenderUsername() {
		return offenderUsername;
	}
	public void setOffenderUsername(String offenderUsername) {
		this.offenderUsername = offenderUsername;
	}
	public String getOffenderUUID() {
		return offenderUUID;
	}
	public void setOffenderUUID(String offenderUUID) {
		this.offenderUUID = offenderUUID;
	}
	public Date getReportDate() {
		return reportDate;
	}
	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> result = new HashMap<>();
		result.put("reporterUsername", reporterUsername);
		result.put("reporterUUID", reporterUUID);
		result.put("offenderUsername", offenderUsername);
		result.put("offenderUUID", offenderUUID);
		result.put("reportDate", dateFormat.format(reportDate));
		result.put("reason", reason);
		return result;
	}

	public String toMessageString() {
		return "Offender: " + offenderUsername + ", Reporter: " + reporterUsername + ", Date: " + dateFormat.format(reportDate) + ", Reason: " + reason; 
	}
}
