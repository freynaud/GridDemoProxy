$(document).ready(function(){

 	 $("#debug").click(function(event){
 	 	alert( $("#firefox").val());
 	 	});
 	
       $("#firefox").keypress(function(event){
       		var keyCode = event.keyCode || event.which;
       		 
  			if (keyCode == 9) {
  				var path = $(this).val();
  				event.preventDefault();
  				$.ajax({
	  				url: "?completion="+path,
	  				type : 'POST',
	  				context: document.body,
	  				success: function(data, textStatus, jqXHR){ 	
	  					$("#firefox").val(jqXHR.responseText);
	  				},
	  				error: function(jqXHR, textStatus, errorThrown){
	  					//alert(jqXHR.responseText);	
	  				}  			
				}); // end ajax
			} else if (keyCode == 13 ){
				var path = $(this).val();
  				event.preventDefault();
  				$.ajax({
	  				url: "?submit="+path,
	  				type : 'POST',
	  				context: document.body,
	  				success: function(data, textStatus, jqXHR){ 	
	  					$("#firefox").val('');
	  					$("#ffs").html(jqXHR.responseText);
	  				},
	  				error: function(jqXHR, textStatus, errorThrown){
	  					//alert(jqXHR.responseText);	
	  				}  			
				}); // end ajax
			} 
       }); // end keypress
       
       
       $('#firefox').keyup(function() {
       		var path = $(this).val();
  			$.ajax({
  					url: "?current="+path,
	  				type : 'POST',
	  				context: document.body,
	  				success: function(data, textStatus, jqXHR){
	  					var result = eval('(' + jqXHR.responseText + ')');
	  					if (result.success){
	  						$("#firefox").val('');
	  					}
	  					$("#info").html(result.info);
	  					$("#ffs").html(result.content);
	  				},
	  				error: function(jqXHR, textStatus, errorThrown){
	  					$("#info").html(jqXHR.responseText);	
	  				}  			
				}); // end ajax
});
       
});

