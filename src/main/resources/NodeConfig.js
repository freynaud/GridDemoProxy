$(document).ready(function(){

 	 $("#debug").click(function(event){
 	 	alert( $("#firefox").val());
 	 	});
 	
 	
 	   var old;
       $("#firefox").keypress(function(event){
       		var keyCode = event.keyCode || event.which;
       		alert(keyCode);
  			if (keyCode == 9) {
  				path = $(this).val();
  				event.preventDefault();
  				if (path != old){
	  				$.ajax({
		  				url: "?completion="+path,
		  				type : 'POST',
		  				context: document.body,
		  				success: function(data, textStatus, jqXHR){ 	
		  					var result = eval('(' + jqXHR.responseText + ')');
		  					$("#info").html(result.info);
		  					$("#firefox").val(result.content);
		  					validatePath(result.content);
		  					old = result.content;
		  					if (result.isDirectory){
		  						old = 'ferret';
		  					}
		  				},
		  				error: function(jqXHR, textStatus, errorThrown){
		  					alert("Affreux. "+jqXHR.responseText);	
		  				}  			
					}); // end ajax	
  				}
			
  				
  			
  				
  				
			} else if (keyCode == 13 ){
				var path = $(this).val();
  				event.preventDefault();
  				$.ajax({
	  				url: "?submit="+path,
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
	  					alert("Affreux. "+jqXHR.responseText);	
	  				}  			
				}); // end ajax
			} 
       }); // end keypress
       
       
        $("#firefox").keyup(function(event){
        	var path = $(this).val();
        	validatePath(path);
       	}); // end keyup
       
       
function validatePath(path){
	$.ajax({
		url: "?current="+path,
		type : 'POST',
		context: document.body,
		success: function(data, textStatus, jqXHR){
			var result = eval('(' + jqXHR.responseText + ')');
			if (result.success){
				$('#info').html(result.content);
			}
			
		},
		error: function(jqXHR, textStatus, errorThrown){
			$('#info').html(jqXHR.responseText);	
		}  			
	}); // end ajax
}
       
});



