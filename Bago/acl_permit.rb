# Permisos basicos
# "1" Para ningun permiso
# "2" Para permiso de busqueda
# "3" Para permiso de lectura
# "4" Para permiso de relacion
# "5" Para permiso de versionado
# "6" Para permiso de escritura
# "7" Para permiso de borrado
# Permisos Extendidos
# "CHANGE_FOLDER_LINKS" Para permiso extendido de linkear carpetas
# "CHANGE_LOCATION" Para permiso extendido de cambiar de ubicacion el documento
# "CHANGE_OWNER" Para permiso extendido de cambiar usuario creador del documento
# "CHANGE_PERMIT" Para permiso extendido de cambiar la seguridad del documento
# "CHANGE_STATE" Para permiso extendido de cambiar el estado (ciclo de vida) del documento
# "DELETE_OBJECT" Para permiso extendido de borrado
# "EXECUTE_PROC" Para permiso extendido de ejecucion de procedimientos
require "java"
include_class "java.util.ArrayList"
class AclPermit
  def getRoles(paso)
  arrlist = ArrayList.new
  if paso == "paso1"
    arrlist.add "dm_world"
    arrlist.add "dm_owner"
    arrlist.add "docbase_owner"
    arrlist.add "sector"
  elsif paso == "paso2"  
    arrlist.add "dm_world"
    arrlist.add "dm_owner"
    arrlist.add "docbase_owner"
  end
  return arrlist
	end
	def getUsuarios(rol)
    if rol == "dm_world"
			"dm_world"
		elsif rol == "dm_owner"
			"dm_owner"
		elsif rol == "docbase_owner"
			"docbase_owner"
		elsif rol == "sector"
			"p_sector"
		end
	end
	def acl_permit(rol,paso)
	  if paso == "paso1"
       if rol == "dm_world"
			   "1"
		   elsif rol == "dm_owner"
			   "3"
			 elsif rol == "docbase_owner"
			   "7"
			 elsif rol == "sector"
			   "3"
			 end
		elsif paso == "paso2"
       if rol == "dm_world"
			   "1"
		   elsif rol == "dm_owner"
			   "3"
			 elsif rol == "docbase_owner"
			   "7"
			 end
    end 
  end
  def acl_permitEx(rol,paso)
	  if paso == "paso1"
       if rol == "dm_world"
			   "EXECUTE_PROC"
		   elsif rol == "dm_owner"
			   "EXECUTE_PROC"
			 elsif rol == "docbase_owner"
			   "DELETE_OBJECT"
		   elsif rol == "sector"
			   "EXECUTE_PROC"
			  end
		elsif paso == "paso2"
       if rol == "dm_world"
			   "EXECUTE_PROC"
		   elsif rol == "dm_owner"
			   "EXECUTE_PROC"
			 elsif rol == "docbase_owner"
			   "DELETE_OBJECT"
			 end
    end 
  end
end